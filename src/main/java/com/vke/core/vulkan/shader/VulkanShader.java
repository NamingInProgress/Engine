package com.vke.core.vulkan.shader;

import com.vke.api.abstraction.IntBitEnum;
import com.vke.api.abstraction.descriptors.ShaderType;
import com.vke.api.abstraction.shader.Shader;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.LogicalDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

public class VulkanShader implements Shader {

    private final ShaderType type;
    private final long handle;
    private final LogicalDevice device;

    public VulkanShader(VKEngine engine, LogicalDevice device, ByteBuffer sourceCode, ShaderType type) {
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkShaderModuleCreateInfo shaderCreateInfo = VkShaderModuleCreateInfo.calloc(stack)
                    .sType$Default()
                    .pCode(sourceCode);

            LongBuffer pShaderModule = stack.mallocLong(1);

            if (VK14.vkCreateShaderModule(device.getDevice(), shaderCreateInfo, null, pShaderModule) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create shader module!"), "SHADER_INIT");
            }
            this.handle = pShaderModule.get(0);
        }

        this.type = type;

    }

    public long getHandle() { return this.handle; }

    @Override
    public ShaderType type() {
        return type;
    }

    public static class Stages implements IntBitEnum<Stages, ShaderType> {

        private int mask;

        public Stages(ShaderType... types) { or(types); }

        @Override
        public Stages or(ShaderType... flags) {
            for (ShaderType type : flags) {
                mask |= type.getVkHandle();
            }
            return this;
        }

        @Override
        public int getVkHandle() {
            return this.mask;
        }

        public static Stages fromString(String[] strings) {
            Stages self = new Stages();

            for (String stage : strings) {
                self.or(ShaderType.fromString(stage));
            }

            return self;
        }

    }

    @Override
    public void free() {
        VK14.vkDestroyShaderModule(device.getDevice(), handle, null);
    }
}
