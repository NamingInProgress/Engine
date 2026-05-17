package com.vke.core.vulkan.descriptor;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorsInfo;
import com.vke.api.rendering.vulkan.descriptors.sets.DescriptorSet;
import com.vke.core.Context;
import com.vke.core.services.shr.ReflectedShader;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.descriptor.data.ShaderDataCreateInfo;
import com.vke.core.vulkan.descriptor.data.ShaderDataManager;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.io.Disposable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EngineDescriptorSetsManager implements Disposable {

    public CompiledDescriptorSetLayout EMPTY_LAYOUT;
    public CompiledDescriptorSetLayout FRAME_DATA_LAYOUT;
    public CompiledDescriptorSetLayout BINDLESS_LAYOUT;

    public DescriptorSet EMPTY;
    public DescriptorSet FRAME_DATA;
    public DescriptorSet BINDLESS;

    public final HashMap<Integer, DescriptorSetLayout> ENGINE_LAYOUTS = new HashMap<>();
    //public final IntObjectHashMap<DescriptorSet> ENGINE_SETS = new IntObjectHashMap<>();

    private final DescriptorAllocator globalSetsAlloc;

    public EngineDescriptorSetsManager(Context context, VulkanRenderer renderer, VulkanRenderDevice device, ReflectedShader truth) {
        this.globalSetsAlloc = new DescriptorAllocator(context.getEngine(), device, ObjectIntHashMap.from(
                new DescriptorType[]{
                        DescriptorType.UNIFORM_BUFFER_DYNAMIC,
                        DescriptorType.COMBINED_IMAGE_SAMPLER
                },
                new int[]{
                        1,
                        4096 // device.capabilities()
                }), 3, 1, true);
        initLayouts(context, device, truth);

        ShaderDataCreateInfo sdci = new ShaderDataCreateInfo(context)
                .device(device)
                .framesInFlight(renderer.getFramesInFlight())
                .minUboAlign(device.capabilities().minUboAlign)
                .frameDataBufferSize(FRAME_DATA_LAYOUT.getLayout().bindings.getFirst().typeLayout.size)
                .maxTexturesCount(4096); // device.capabilities()

        ShaderDataManager.initialize(sdci);
    }

    private void initLayouts(Context context, VulkanRenderDevice device, ReflectedShader truth) {
        for (Map.Entry<ReflectedShader.ResourceType, ArrayList<ReflectedShader.DescriptorResource>> entry : truth.getDescriptors().entrySet()) {
            for (ReflectedShader.DescriptorResource descriptorResource : entry.getValue()) {
                ENGINE_LAYOUTS.computeIfAbsent(descriptorResource.set, (_) -> new DescriptorSetLayout()).bindings.add(BindingLayout.fromDescriptorResource(descriptorResource, entry.getKey(), truth.getMetadata().staticBuffers().contains(descriptorResource.name)));
            }
        }

        //this.EMPTY_LAYOUT = new CompiledDescriptorSetLayout(context.getEngine(), device, empty, null);
        //this.FRAME_DATA_LAYOUT = new CompiledDescriptorSetLayout(context.getEngine(), device, frameData, null);
        //this.BINDLESS_LAYOUT = new CompiledDescriptorSetLayout(context.getEngine(), device, bindless, null);

        //DescriptorsInfo info = new DescriptorsInfo();
        //info.dynamicBuffers.add("camera");
        //info.runtimeSizeArraySizes.put("textures", 4096); //device.capabilities()
        //this.EMPTY = new DescriptorSet(globalSetsAlloc.allocate(EMPTY_LAYOUT), device, context.getEngine(), empty, info);
        //this.FRAME_DATA = new DescriptorSet(globalSetsAlloc.allocate(FRAME_DATA_LAYOUT), device, context.getEngine(), frameData, info);
        //this.BINDLESS = new DescriptorSet(globalSetsAlloc.allocate(BINDLESS_LAYOUT, 4096), device, context.getEngine(), bindless, info);
    }

    public HashMap<Integer, DescriptorSetLayout> getDefaults(ArrayList<ReflectedShader> shaders) {
        HashMap<Integer, DescriptorSetLayout> map = new HashMap<>();
        map.put(0, EMPTY_LAYOUT.getLayout());
        map.put(1, EMPTY_LAYOUT.getLayout());
        return null;
    }

    @Override
    public void free() {
        this.EMPTY_LAYOUT.free();
        this.FRAME_DATA_LAYOUT.free();
        this.BINDLESS_LAYOUT.free();
        this.globalSetsAlloc.free();
        ShaderDataManager.getInstance().free();
    }
}
