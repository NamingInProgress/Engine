package com.vke.core.vulkan.shader;

import com.vke.api.abstraction.descriptors.ShaderType;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

import java.util.HashSet;

public class VKShaderProgram implements Disposable {

    private final Shader[] shaders;
    private final AutoHeapAllocator alloc;

    public VKShaderProgram(Shader... shaders) {
        this.alloc = new AutoHeapAllocator();

        HashSet<ShaderType> types = new HashSet<>();
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
                    .stage(shader.getType().getVkHandle())
                    .module(shader.getHandle())
                    .pName(MemoryUtil.memUTF8("main", true));
        }

        return infos;
    }

    @Override
    public void free() {
        for (Shader s : shaders) {
            s.free();
        }
        alloc.close();
    }

}
