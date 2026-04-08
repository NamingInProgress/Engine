package com.vke.core.vulkan.shader;

import com.vke.api.rendering.abstraction.enums.ShaderType;
import com.vke.api.rendering.abstraction.shader.ShaderProgram;
import com.vke.core.Context;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.utils.io.Disposable;
import com.vke.utils.iter.Iter;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

import java.io.IOException;
import java.util.HashSet;

public class VKShaderProgram implements Disposable {

    private final VulkanShader[] shaders;
    private final AutoHeapAllocator alloc;

    public VKShaderProgram(VulkanShader... shaders) {
        this.alloc = new AutoHeapAllocator();

        HashSet<ShaderType> types = new HashSet<>();
        for (VulkanShader shader : shaders) {
            types.add(shader.type());
        }
        if (types.size() != shaders.length) {
            throw new RuntimeException("No duplicate shader type allowed for one program!");
        }

        this.shaders = shaders;
    }

    public static VKShaderProgram asVkShaderProgram(Context context, ShaderProgram sp) {
        return new VKShaderProgram(Iter.of(sp.getShaders()).faultyMap(ah -> ah.acquire(context)).<VulkanShader>cast().toArray());
    }

    public VulkanShader[] getShaders() {
        return shaders;
    }

    public VkPipelineShaderStageCreateInfo[] getShaderCreateInfos() {
        VkPipelineShaderStageCreateInfo[] infos = new VkPipelineShaderStageCreateInfo[shaders.length];
        for (int i = 0; i < shaders.length; i++) {
            VulkanShader shader = shaders[i];
            infos[i] = alloc.allocStruct(VkPipelineShaderStageCreateInfo.SIZEOF, VkPipelineShaderStageCreateInfo::new)
                    .sType$Default()
                    .stage(shader.type().getVkHandle())
                    .module(shader.getHandle())
                    .pName(MemoryUtil.memUTF8("main", true));
        }

        return infos;
    }

    @Override
    public void free() {
        for (VulkanShader s : shaders) {
            s.free();
        }
        alloc.close();
    }

}
