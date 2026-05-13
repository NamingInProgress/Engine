package com.vke.api.rendering.vulkan.pipeline;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.abstraction.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.shader.ShaderProgram;
import com.vke.api.rendering.vulkan.descriptors.BaseType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorSets;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorsInfo;
import com.vke.api.rendering.vulkan.descriptors.types.PrimitiveType;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantLayout;
import com.vke.api.rendering.vulkan.pushconstants.PushConstants;
import com.vke.core.Context;
import com.vke.core.services2.Services;
import com.vke.core.vulkan.shr.ReflectedShader;
import com.vke.core.vulkan.shr.service.ShaderReflector;
import com.vke.core.vulkan.shr.service.ShaderReflectorImpl;
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

    // Info create methods that are shared between compute and render pipelines
    default DescriptorSets createDescriptorSets(Context ctx, VulkanRenderDevice device, DescriptorsInfo additionalDescInfo, ArrayList<ReflectedShader> shaders) {
        HashMap<Integer, DescriptorSetLayout> sets = new HashMap<>();

        for (ReflectedShader shader : shaders) {
            var reflectedDescriptors = shader.getDescriptors();

            for (ArrayList<ReflectedShader.DescriptorResource> value : reflectedDescriptors.values()) {
                for (ReflectedShader.DescriptorResource resource : value) {
                    if (!sets.containsKey(resource.set)) sets.put(resource.set, new DescriptorSetLayout());
                }
            }

            for (Map.Entry<ReflectedShader.ResourceType, ArrayList<ReflectedShader.DescriptorResource>> entry : reflectedDescriptors.entrySet()) {
                for (ReflectedShader.DescriptorResource resource : entry.getValue()) {
                    DescriptorSetLayout descriptor = sets.computeIfAbsent(resource.set, (_) -> new DescriptorSetLayout());
                    descriptor.set = resource.set;
                    if (resource.name.equals("TransformBuffer") || resource.name.equals("ClipBuffer")) {
                        System.out.println("Something");
                    }

                    BindingLayout binding = new BindingLayout();
                    binding.name = resource.name;
                    binding.set = resource.set;
                    binding.binding = resource.binding;
                    binding.descriptorCount = Arrays.stream(resource.arrayDim).reduce(1, (a, b) -> a * b);
                    binding.isDynamic = additionalDescInfo.dynamicBuffers.contains(resource.name);
                    binding.type = DescriptorType.fromBaseType(entry.getKey(), binding.isDynamic);

                    if (resource.struct == null) {
                        try {
                            PrimitiveType primitiveType = new PrimitiveType();
                            primitiveType.scalarType = BaseType.fromPipelineBaseType(resource.baseType);
                            primitiveType.vecSize = resource.vecSize;
                            primitiveType.size = resource.baseType.byteSize() * primitiveType.vecSize;
                            binding.typeLayout = primitiveType;
                        } catch (RuntimeException ignore) {}
                    } else {
                        binding.typeLayout = resource.struct;
                    }
                    System.out.println("this could be null and if its null we are fucked: " + binding.typeLayout);
                    binding.packingType = PackingType.fromDescriptorType(binding.type);

                    descriptor.bindings.add(binding);
                }
            }
        }

        return new DescriptorSets(ctx.getEngine(), device, (ArrayList<DescriptorSetLayout>) Iter.of(sets.values()).collectToList(), additionalDescInfo);
    }

    default PushConstants createPushConstants(ArrayList<ReflectedShader> shaders) {
        PushConstantLayout layout = null;
        for (ReflectedShader shader : shaders) {
            ReflectedShader.PushConstantsResource pc = shader.getPushConstants();
            if (pc == null) continue;

            layout = new PushConstantLayout(pc.name, 0, pc.size, pc.struct, PackingType.STD140);
        }

        if (layout == null) layout = new PushConstantLayout("", 0, 0, null, PackingType.STD140);

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

}
