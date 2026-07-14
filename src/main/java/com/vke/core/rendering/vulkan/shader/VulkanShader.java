package com.vke.core.rendering.vulkan.shader;

import com.vke.api.rendering.abstraction.renderer.IntBitEnum;
import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.api.rendering.abstraction.renderer.shader.Shader;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

public class VulkanShader implements Shader {

    private final ShaderType type;
    private final long handle;
    private final VulkanRenderSystem ctx;
    private final long id;
    private boolean hasBeenFreed;

    public VulkanShader(VulkanRenderSystem ctx, ByteBuffer sourceCode, ShaderType type, long id) {
        this.id = id;
        this.ctx = ctx;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkShaderModuleCreateInfo shaderCreateInfo = VkShaderModuleCreateInfo.calloc(stack)
                    .sType$Default()
                    .pCode(sourceCode);

            LongBuffer pShaderModule = stack.mallocLong(1);

            if (VK14.vkCreateShaderModule(ctx.device().vkLogicalDevice(), shaderCreateInfo, null, pShaderModule) != VK14.VK_SUCCESS) {
                ctx.throwException(new IllegalStateException("Failed to create shader module!"), "SHADER_INIT");
            }
            this.handle = pShaderModule.get(0);
        }

        this.type = type;

    }

    public long getHandle() { return this.handle; }

    public long getShaderID() { return this.id; }

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
        if (!hasBeenFreed) {
            VK14.vkDestroyShaderModule(ctx.device().vkLogicalDevice(), handle, null);
        }
        hasBeenFreed = true;
    }
}
