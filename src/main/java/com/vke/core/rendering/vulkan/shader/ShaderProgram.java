package com.vke.core.rendering.vulkan.shader;

import com.vke.core.memory.AutoHeapAllocator;
import com.vke.utils.Disposable;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

import java.util.HashSet;

public class ShaderProgram implements Disposable {

    private final Shader[] shaders;
    private final AutoHeapAllocator alloc;

    public ShaderProgram(Shader... shaders) {
        this.alloc = new AutoHeapAllocator();

        HashSet<Shader.Type> types = new HashSet<>();
        for (Shader shader : shaders) {
            types.add(shader.getType());
        }
        if (types.size() != shaders.length) {
            throw new RuntimeException("No duplicate shader type allowed for one program!");
        }

        this.shaders = shaders;
    }

    public VkPipelineShaderStageCreateInfo[] getShaderCreateInfos() {
        VkPipelineShaderStageCreateInfo[] infos = new VkPipelineShaderStageCreateInfo[shaders.length];
        for (int i = 0; i < shaders.length; i++) {
            Shader shader = shaders[i];
            infos[i] = alloc.allocStruct(VkPipelineShaderStageCreateInfo.SIZEOF, VkPipelineShaderStageCreateInfo::new)
                    .sType$Default()
                    .stage(shader.getType().getVkStageInt())
                    .module(shader.getHandle())
                    .pName(alloc.utf8("main").getHeapObject());
        }

        return infos;
    }

    @Override
    public void free() {
        alloc.close();
    }

}
