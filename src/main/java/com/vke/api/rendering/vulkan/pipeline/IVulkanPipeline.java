package com.vke.api.rendering.vulkan.pipeline;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.abstraction.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.pipeline.PipelineLayout;
import com.vke.api.rendering.abstraction.shader.ShaderProgram;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantLayout;
import com.vke.api.rendering.vulkan.pushconstants.PushConstants;
import com.vke.core.Context;
import com.vke.core.services2.Services;
import com.vke.core.vulkan.pipeline.VulkanPipelineLayout;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.core.vulkan.service.VulkanRendererAPI;
import com.vke.core.vulkan.shr.ReflectedShader;
import com.vke.core.vulkan.shr.service.ShaderReflector;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.shader.VKShaderProgram;
import com.vke.core.vulkan.shader.VulkanShader;
import com.vke.utils.Utils;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

import java.util.*;

public interface IVulkanPipeline extends Pipeline {

    long getHandle();
    PipelineLayout layout();

    // Info create methods that are shared between compute and render pipelines
    default List<DescriptorSetLayout> createDescriptorSets(Context ctx, VulkanRenderDevice device, ArrayList<ReflectedShader> shaders) {
        HashMap<Integer, DescriptorSetLayout> sets = new HashMap<>();

        for (ReflectedShader shader : shaders) {
            var reflectedDescriptors = shader.getDescriptors();

            for (Map.Entry<ReflectedShader.ResourceType, ArrayList<ReflectedShader.DescriptorResource>> entry : reflectedDescriptors.entrySet()) {
                for (ReflectedShader.DescriptorResource resource : entry.getValue()) {
                    DescriptorSetLayout descriptor = sets.computeIfAbsent(resource.set, (_) -> new DescriptorSetLayout());

                    BindingLayout binding = BindingLayout.fromDescriptorResource(resource, entry.getKey(),
                            shader.getMetadata().staticBuffers().contains(resource.name));

                    descriptor.bindings.add(binding);
                }
            }
        }

        ArrayList<Integer> mismatchedSets = new ArrayList<>();
        var mgr = ctx.<VulkanRendererAPI>service(Services.VULKAN_RENDERER).<VulkanRenderer>assumeImplementation().getEngineSetsManager();
        var engineSets = mgr.getDefaults();
        for (Map.Entry<Integer, DescriptorSetLayout> integerDescriptorSetLayoutEntry : engineSets.entrySet()) {
            if (!integerDescriptorSetLayoutEntry.getValue().equals(sets.get(integerDescriptorSetLayoutEntry.getKey()))) {
                mismatchedSets.add(integerDescriptorSetLayoutEntry.getKey());
            }
        }

        sets.putAll(engineSets);

        return sets.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .toList();
    }

    default PushConstants createPushConstants(ArrayList<ReflectedShader> shaders) {
        PushConstantLayout layout = null;
        for (ReflectedShader shader : shaders) {
            ReflectedShader.PushConstantsResource pc = shader.getPushConstants();
            if (pc == null) continue;

            layout = new PushConstantLayout(pc.name, 0, pc.size, pc.struct, PackingType.STD140);
        }

        return new PushConstants(layout);
    }

    default ArrayList<ReflectedShader> getReflectedShaders(Context context, VKShaderProgram program) {
        ShaderReflector refl = context.service(Services.SHADER_REFLECTION);
        ArrayList<ReflectedShader> reflectedShaders = new ArrayList<>();

        for (Long id : Iter.of(program.getShaders()).map(VulkanShader::getShaderID)) {
            Option<ReflectedShader> shaderOpt = refl.get(id);
            if (shaderOpt.isNone()) {
                throw new IllegalStateException("Requested reflected shader but none was found! This error should not happen");
            }
            reflectedShaders.add(shaderOpt.unwrap());
        }

        return reflectedShaders;
    }

    default VkPipelineShaderStageCreateInfo.Buffer getShaderStages(MemoryStack stack, ShaderProgram shaderProgram, VKShaderProgram compiledShaderProgram) {
        VkPipelineShaderStageCreateInfo.Buffer stages =
                VkPipelineShaderStageCreateInfo.calloc(shaderProgram.getShaderCount(), stack);

        VkPipelineShaderStageCreateInfo[] shaderStageCreateInfos = compiledShaderProgram.getShaderCreateInfos();

        for (int i = 0; i < shaderStageCreateInfos.length; i++) {
            VkPipelineShaderStageCreateInfo stage = shaderStageCreateInfos[i];

            stages.get(i).sType$Default()
                    .stage(stage.stage())
                    .module(stage.module())
                    .pName(Utils.ensureCStr(stage.pName()));
        }
        return stages;
    }

    default <T extends UniformHandle> T uniform(String name) {
        return ((VulkanPipelineLayout) layout()).getGroup().resolve(name);
    }

}
