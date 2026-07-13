package com.vke.core.vulkan.descriptor;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.vulkan.descriptors.bindings.BufferBinding;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.FieldHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.CISHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.ImageHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.SamplerHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.CISArrayHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.ImageArrayHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.SamplerArrayHandle;
import com.vke.core.Context;
import com.vke.core.vulkan.draw.VulkanFrameDataManager;
import com.vke.core.rendering.texture.VulkanTextureManager;
import com.vke.core.rendering.vertexconsumer.RecyclerArrayList;
import com.vke.core.vulkan.buffers.MappedGpuRingBuffer;
import com.vke.core.vulkan.descriptor.ds2.DescriptorSetInstance;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.pipeline.VulkanPipelineLayout;
import com.vke.core.vulkan.service.VulkanRenderSystem;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.core.vulkan.shr.ReflectedShader;
import com.vke.utils.io.Disposable;
import com.vke.utils.iter.Iter;
import com.vke.utils.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class EngineDescriptorSetsManager implements Disposable {

    public final HashMap<Integer, DescriptorSetLayout> ENGINE_LAYOUTS = new HashMap<>();

    public final ArrayList<DescriptorSetInstance> INSTANCES = new ArrayList<>();

    public final VulkanTextureManager textureManager;
    public VulkanFrameDataManager frameDataManager;

    public VulkanPipelineLayout ENGINE_PIPELINE_LAYOUT;

    public Integer[] usedSets;
    public int highestSet; // All sets below this one are used!

    private final HashMap<Pair<VulkanPipelineLayout, UniformHandle>, VulkanRenderer.IntWrapper> scheduledBindingUpdates = new HashMap<>();
    private final RecyclerArrayList<Pair<VulkanPipelineLayout, UniformHandle>> toRemoveBindingUpdates = new RecyclerArrayList<>(20);

    public EngineDescriptorSetsManager(VulkanRenderSystem ctx, ReflectedShader truth) {
        this.textureManager = new VulkanTextureManager(ctx, this, ctx.renderer().getBindlessTexturesCount());
        ctx.getEngine().EVENT_BUS.register(textureManager);

        for (Map.Entry<ReflectedShader.ResourceType, ArrayList<ReflectedShader.DescriptorResource>> entry : truth.getDescriptors().entrySet()) {
            for (ReflectedShader.DescriptorResource descriptorResource : entry.getValue()) {
                if (descriptorResource.set > highestSet) highestSet = descriptorResource.set;
                var set = ENGINE_LAYOUTS.computeIfAbsent(descriptorResource.set, (_) -> new DescriptorSetLayout());

                var layout = BindingLayout.fromDescriptorResource(descriptorResource, entry.getKey(),
                        truth.getMetadata().staticBuffers().contains(descriptorResource.name));
                layout.resolveRuntimeSizeArrays(truth.getMetadata().defaultRuntimeSizes());
                set.bindings.add(layout);
            }
        }
        usedSets = Iter.of(ENGINE_LAYOUTS.keySet()).toArray();
    }

    public void makeFrameDataManager() {
        this.frameDataManager = new VulkanFrameDataManager(this);
    }

    public List<Integer> getDynamicOffsets() {
        return ENGINE_PIPELINE_LAYOUT.getSets().stream()
                .flatMap(instance -> instance.bindings.values().stream()
                        .filter(binding -> binding instanceof BufferBinding)
                        .map(binding -> ((BufferBinding) binding).buffer)
                        .filter(buf -> buf instanceof MappedGpuRingBuffer)
                        .map(buf -> (int) ((MappedGpuRingBuffer) buf).getOffset())
                        .sorted(Comparator.comparingInt(c -> c))
                ).collect(Collectors.toCollection(ArrayList::new));
    }

    public void onFrame() {
        toRemoveBindingUpdates.clear();
        for (var entry : scheduledBindingUpdates.entrySet()) {
            if (entry.getValue().anInt > 0) {
                writeHandle(entry.getKey().v1, entry.getKey().v2);
                entry.getValue().anInt--;
            } else {
                toRemoveBindingUpdates.add(entry.getKey());
            }
        }
        toRemoveBindingUpdates.iter().forEach(scheduledBindingUpdates::remove);
    }

    public void scheduleDescriptorUpdate(VulkanPipelineLayout layout, UniformHandle handle, FrameCounter frameCounter) {
        scheduledBindingUpdates.put(new Pair<>(layout, handle), new VulkanRenderer.IntWrapper(frameCounter.framesInFlight() + 1));
    }

    public HashMap<Integer, DescriptorSetLayout> getDefaults() {
        return ENGINE_LAYOUTS;
    }

    @Override
    public void free() {
        ENGINE_PIPELINE_LAYOUT.free();
    }

    public void writeHandle(VulkanPipelineLayout layout, UniformHandle uh) {
        long dsh = layout.getSetHandle(uh.set);
        var writer = layout.writer;
        switch (uh) {
            case CISHandle handle ->
                    writer.writeCombinedImageSamplers(dsh, handle.binding, handle.cisBinding.views, handle.cisBinding.samplers);
            case CISArrayHandle handle ->
                    writer.writeCombinedImageSamplers(dsh, handle.binding, handle.cisBinding.views, handle.cisBinding.samplers);
            case ImageHandle handle ->
                    writer.writeImages(dsh, handle.binding, handle.imgBinding.views, handle.type);
            case ImageArrayHandle handle ->
                    writer.writeImages(dsh, handle.binding, handle.imgBinding.views, handle.type);
            case SamplerHandle handle ->
                    writer.writeSamplers(dsh, handle.binding, handle.samplBinding.samplers);
            case SamplerArrayHandle handle ->
                    writer.writeSamplers(dsh, handle.binding, handle.samplBinding.samplers);
            case BufferHandle handle ->
                    writer.writeBuffer(dsh, handle.binding, handle.bufferSize, handle.offset, handle.gpuAddress, handle.type);
            case FieldHandle handle ->
                    writer.writeBuffer(dsh, handle.binding, handle.parent.bufferSize, handle.parent.offset, handle.parent.gpuAddress, handle.type);
            default -> {}
        }
        writer.flush();
    }

}
