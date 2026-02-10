package com.vke.core.rendering.vulkan.shader;

import com.vke.api.vulkan.VkBitEnum;
import com.vke.api.vulkan.VkEnum;
import com.vke.api.vulkan.descriptors.DescriptorData;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.utils.Disposable;
import com.vke.utils.Utils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

public class Shader implements Disposable {

    private final Type type;
    private final long handle;
    private final LogicalDevice device;

    public Shader(VKEngine engine, LogicalDevice device, ByteBuffer sourceCode, Type type) {
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

    public Type getType() {
        return type;
    }

    public enum Type implements VkEnum{
        VERTEX(Shaderc.shaderc_vertex_shader, "vertex", "vert"),
        FRAGMENT(Shaderc.shaderc_fragment_shader, "fragment", "frag"),
        COMPUTE(Shaderc.shaderc_compute_shader, "compute", "comp");

        private final int shadercHandle;

        private final String[] names;

        Type(int shadercHandle, String... names) {
            this.shadercHandle = shadercHandle;
            this.names = names;
        }

        public int getShadercHandle() {
            return this.shadercHandle;
        }

        public int getVkHandle() {
            return switch (this) {
                case VERTEX -> VK14.VK_SHADER_STAGE_VERTEX_BIT;
                case FRAGMENT -> VK14.VK_SHADER_STAGE_FRAGMENT_BIT;
                case COMPUTE -> VK14.VK_SHADER_STAGE_COMPUTE_BIT;
            };
        }

        public String[] getNames() { return this.names; }

        public static Type fromString(String s) {
            return Arrays.stream(Type.values()).filter(c -> Utils.arrayContains(c.getNames(), s)).findFirst().orElse(null);
        }

    }

    public static class Stages implements VkBitEnum<Stages, Type> {

        private int mask;

        public Stages(Type... types) { or(types); }

        @Override
        public Stages or(Type... flags) {
            for (Type type : flags) {
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
                self.or(Type.fromString(stage));
            }

            return self;
        }

    }

    @Override
    public void free() {
        VK14.vkDestroyShaderModule(device.getDevice(), handle, null);
    }
}
