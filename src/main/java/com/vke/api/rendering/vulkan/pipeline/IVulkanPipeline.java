package com.vke.api.rendering.vulkan.pipeline;

import com.vke.api.rendering.abstraction.renderer.enums.buffer.PackingType;
import com.vke.api.rendering.abstraction.renderer.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.PipelineLayout;
import com.vke.api.rendering.abstraction.renderer.shader.ShaderProgram;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantLayout;
import com.vke.api.rendering.vulkan.pushconstants.PushConstants;
import com.vke.core.Context;
import com.vke.core.rendering.reflection2.api.DescriptorResource;
import com.vke.core.rendering.reflection2.api.PushConstantsResource;
import com.vke.core.rendering.reflection2.api.ReflectedShader2;
import com.vke.core.rendering.reflection2.service.ShaderReflector2;
import com.vke.core.services2.Services;
import com.vke.core.rendering.vulkan.pipeline.VulkanPipelineLayout;
import com.vke.core.rendering.vulkan.service.VulkanRenderer;
import com.vke.core.rendering.vulkan.service.VulkanRendererAPI;
import com.vke.core.rendering.vulkan.shader.VKShaderProgram;
import com.vke.core.rendering.vulkan.shader.VulkanShader;
import com.vke.utils.Utils;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

import java.util.*;

//official v22 certificate to leave this I
public interface IVulkanPipeline extends Pipeline {

    long getHandle();
    PipelineLayout layout();

    // Info create methods that are shared between compute and render pipelines
    default List<DescriptorSetLayout> createDescriptorSets(Context ctx, ArrayList<ReflectedShader2> shaders) {
        HashMap<Integer, DescriptorSetLayout> sets = new HashMap<>();

        for (ReflectedShader2 shader : shaders) {
            var reflectedDescriptors = shader.descriptors();

            for (var entry : reflectedDescriptors.entrySet()) {
                for (DescriptorResource resource : entry.getValue()) {
                    DescriptorSetLayout descriptor = sets.computeIfAbsent(resource.set, (_) -> new DescriptorSetLayout());

                    BindingLayout binding = BindingLayout.fromDescriptorResource(resource, entry.getKey(),
                            shader.getMetadata().staticBuffers().contains(resource.name));

                    binding.resolveRuntimeSizeArrays(shader.getMetadata().defaultRuntimeSizes());

                    descriptor.bindings.add(binding);
                }
            }
        }

        var mgr = ctx.<VulkanRendererAPI>service(Services.RENDERER).<VulkanRenderer>assumeImplementation().getEngineSetsManager();
        var engineSets = mgr.getDefaults();

        sets.putAll(engineSets);

        return sets.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .toList();
    }

    default PushConstants createPushConstants(ArrayList<ReflectedShader2> shaders) {
        PushConstantLayout layout = null;
        for (ReflectedShader2 shader : shaders) {
            PushConstantsResource pc = shader.pushConstants();
            if (pc == null) continue;

            layout = new PushConstantLayout(pc.name, 0, pc.size, pc.struct, PackingType.STD140);
        }

        return layout == null ? null : new PushConstants(layout);
    }

    default ArrayList<ReflectedShader2> getReflectedShaders(Context context, VKShaderProgram program) {
        ShaderReflector2 refl = context.service(Services.SHADER_REFLECTION2);
        ArrayList<ReflectedShader2> reflectedShaders = new ArrayList<>();

        for (Long id : Iter.of(program.getShaders()).map(VulkanShader::getShaderID)) {
            Option<ReflectedShader2> shaderOpt = refl.get(id);
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
