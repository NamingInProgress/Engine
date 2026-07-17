package com.vke.api.rendering.abstraction.renderer.enums;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.utils.Utils;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.VK14;

import java.util.Arrays;

public enum ShaderType implements IntEnum {
    VERTEX(Shaderc.shaderc_vertex_shader, "vertex", "vert"),
    FRAGMENT(Shaderc.shaderc_fragment_shader, "fragment", "frag"),
    COMPUTE(Shaderc.shaderc_compute_shader, "compute", "comp");

    private final int shadercHandle;

    private final String[] names;

    ShaderType(int shadercHandle, String... names) {
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

    public String[] getNames() {
        return this.names;
    }

    public static ShaderType fromString(String s) {
        return Arrays.stream(ShaderType.values()).filter(c -> Utils.arrayContains(c.getNames(), s)).findFirst().orElse(null);
    }

}
