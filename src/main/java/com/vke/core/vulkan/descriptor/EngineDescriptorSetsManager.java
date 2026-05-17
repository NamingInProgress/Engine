package com.vke.core.vulkan.descriptor;

import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.core.Context;
import com.vke.core.vulkan.descriptor.data.ShaderDataManager;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.core.vulkan.shr.ReflectedShader;
import com.vke.utils.io.Disposable;
import com.vke.utils.iter.Iter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EngineDescriptorSetsManager implements Disposable {

    public final HashMap<Integer, DescriptorSetLayout> ENGINE_LAYOUTS = new HashMap<>();
    public Integer[] usedSets;

    public EngineDescriptorSetsManager(Context context, VulkanRenderer renderer, VulkanRenderDevice device, ReflectedShader truth) {
        for (Map.Entry<ReflectedShader.ResourceType, ArrayList<ReflectedShader.DescriptorResource>> entry : truth.getDescriptors().entrySet()) {
            for (ReflectedShader.DescriptorResource descriptorResource : entry.getValue()) {
                ENGINE_LAYOUTS.computeIfAbsent(descriptorResource.set, (_) -> new DescriptorSetLayout()).bindings.add(BindingLayout.fromDescriptorResource(descriptorResource, entry.getKey(), truth.getMetadata().staticBuffers().contains(descriptorResource.name)));
            }
        }
        usedSets = Iter.of(ENGINE_LAYOUTS.keySet()).toArray();
    }

    public HashMap<Integer, DescriptorSetLayout> getDefaults() {
        return ENGINE_LAYOUTS;
    }

    @Override
    public void free() {
        ShaderDataManager.getInstance().free();
    }
}
