package com.vke.api.rendering.abstraction.renderer.enums.texture;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.api.rendering.vulkan.pipeline.BaseType;
import com.vke.utils.iter.helpers.Option;
import org.lwjgl.vulkan.VK14;

public enum Format implements IntEnum {

    UNDEFINED(VK14.VK_FORMAT_UNDEFINED),

    // 8 bit normalized
    R8(VK14.VK_FORMAT_R8_UNORM),
    RG8(VK14.VK_FORMAT_R8G8_UNORM),
    RGB8(VK14.VK_FORMAT_R8G8B8_UNORM),
    RGBA8(VK14.VK_FORMAT_R8G8B8A8_UNORM),

    // sRGB variants
    R8_SRGB(VK14.VK_FORMAT_R8_SRGB),
    RG8_SRGB(VK14.VK_FORMAT_R8G8_SRGB),
    RGB8_SRGB(VK14.VK_FORMAT_R8G8B8_SRGB),
    RGBA8_SRGB(VK14.VK_FORMAT_R8G8B8A8_SRGB),

    // Native Swapchain Formats
    BGRA8(VK14.VK_FORMAT_B8G8R8A8_UNORM),
    BGRA8_SRGB(VK14.VK_FORMAT_B8G8R8A8_SRGB),

    // Floating point formats
    R16F(VK14.VK_FORMAT_R16_SFLOAT),
    RG16F(VK14.VK_FORMAT_R16G16_SFLOAT),
    RGB16F(VK14.VK_FORMAT_R16G16B16_SFLOAT),
    RGBA16F(VK14.VK_FORMAT_R16G16B16A16_SFLOAT),

    R32F(VK14.VK_FORMAT_R32_SFLOAT),
    RG32F(VK14.VK_FORMAT_R32G32_SFLOAT),
    RGB32F(VK14.VK_FORMAT_R32G32B32_SFLOAT),
    RGBA32F(VK14.VK_FORMAT_R32G32B32A32_SFLOAT),

    R64F(VK14.VK_FORMAT_R64_SFLOAT),
    RG64F(VK14.VK_FORMAT_R64G64_SFLOAT),
    RGB64F(VK14.VK_FORMAT_R64G64B64_SFLOAT),
    RGBA64F(VK14.VK_FORMAT_R64G64B64A64_SFLOAT),

    // UINT
    R8UI(VK14.VK_FORMAT_R8_UINT),
    RG8UI(VK14.VK_FORMAT_R8G8_UINT),
    RGB8UI(VK14.VK_FORMAT_R8G8B8_UINT),
    RGBA8UI(VK14.VK_FORMAT_R8G8B8A8_UINT),

    R16UI(VK14.VK_FORMAT_R16_UINT),
    RG16UI(VK14.VK_FORMAT_R16G16_UINT),
    RGB16UI(VK14.VK_FORMAT_R16G16B16_UINT),
    RGBA16UI(VK14.VK_FORMAT_R16G16B16A16_UINT),

    R32UI(VK14.VK_FORMAT_R32_UINT),
    RG32UI(VK14.VK_FORMAT_R32G32_UINT),
    RGB32UI(VK14.VK_FORMAT_R32G32B32_UINT),
    RGBA32UI(VK14.VK_FORMAT_R32G32B32A32_UINT),

    R64UI(VK14.VK_FORMAT_R64_UINT),
    RG64UI(VK14.VK_FORMAT_R64G64_UINT),
    RGB64UI(VK14.VK_FORMAT_R64G64B64_UINT),
    RGBA64UI(VK14.VK_FORMAT_R64G64B64A64_UINT),

    // INT
    R8I(VK14.VK_FORMAT_R8_SINT),
    RG8I(VK14.VK_FORMAT_R8G8_SINT),
    RGB8I(VK14.VK_FORMAT_R8G8B8_SINT),
    RGBA8I(VK14.VK_FORMAT_R8G8B8A8_SINT),

    R16I(VK14.VK_FORMAT_R16_SINT),
    RG16I(VK14.VK_FORMAT_R16G16_SINT),
    RGB16I(VK14.VK_FORMAT_R16G16B16_SINT),
    RGBA16I(VK14.VK_FORMAT_R16G16B16A16_SINT),

    R32I(VK14.VK_FORMAT_R32_SINT),
    RG32I(VK14.VK_FORMAT_R32G32_SINT),
    RGB32I(VK14.VK_FORMAT_R32G32B32_SINT),
    RGBA32I(VK14.VK_FORMAT_R32G32B32A32_SINT),

    R64I(VK14.VK_FORMAT_R64_SINT),
    RG64I(VK14.VK_FORMAT_R64G64_SINT),
    RGB64I(VK14.VK_FORMAT_R64G64B64_SINT),
    RGBA64I(VK14.VK_FORMAT_R64G64B64A64_SINT),

    // DEPTH & STENCIL
    DEPTH16(VK14.VK_FORMAT_D16_UNORM),
    DEPTH32F(VK14.VK_FORMAT_D32_SFLOAT),

    DEPTH24_STENCIL8(VK14.VK_FORMAT_D24_UNORM_S8_UINT),
    DEPTH32F_STENCIL8(VK14.VK_FORMAT_D32_SFLOAT_S8_UINT),

    STENCIL8(VK14.VK_FORMAT_S8_UINT);

    private final int handle;

    Format(int handle) {
        this.handle = handle;
    }

    @Override
    public int getIntVal() {
        return this.handle;
    }

    public static Option<Format> valueOfOption(String name) {
        return Option.useIfNotFaulty(() -> Format.valueOf(name));
    }

    public boolean isDepth() {
        return this == DEPTH16 || this == DEPTH32F || this == DEPTH24_STENCIL8 || this == DEPTH32F_STENCIL8;
    }

    public boolean isStencil() {
        return this == STENCIL8 || this == DEPTH24_STENCIL8 || this == DEPTH32F_STENCIL8;
    }

    public static Format fromBaseType(BaseType type, int size) {
        return switch (type) {
            // --- FLOAT ---
            case F16 -> switch (size) {
                case 1 -> Format.R16F;
                case 2 -> Format.RG16F;
                case 3 -> Format.RGB16F;
                case 4 -> Format.RGBA16F;
                default -> throw new IllegalStateException("Vertex Attribute size " + size + " is invalid");
            };
            case F32 -> switch (size) {
                case 1 -> Format.R32F;
                case 2 -> Format.RG32F;
                case 3 -> Format.RGB32F;
                case 4 -> Format.RGBA32F;
                default -> throw new IllegalStateException("Vertex Attribute size " + size + " is invalid");
            };
            case F64 -> switch (size) {
                case 1 -> Format.R64F;
                case 2 -> Format.RG64F;
                case 3 -> Format.RGB64F;
                case 4 -> Format.RGBA64F;
                default -> throw new IllegalStateException("Vertex Attribute size " + size + " is invalid");
            };

            // --- SIGNED INT ---
            case I8 -> switch (size) {
                case 1 -> Format.R8I;
                case 2 -> Format.RG8I;
                case 3 -> Format.RGB8I;
                case 4 -> Format.RGBA8I;
                default -> throw new IllegalStateException("Vertex Attribute size " + size + " is invalid");
            };
            case I16 -> switch (size) {
                case 1 -> Format.R16I;
                case 2 -> Format.RG16I;
                case 3 -> Format.RGB16I;
                case 4 -> Format.RGBA16I;
                default -> throw new IllegalStateException("Vertex Attribute size " + size + " is invalid");
            };
            case I32 -> switch (size) {
                case 1 -> Format.R32I;
                case 2 -> Format.RG32I;
                case 3 -> Format.RGB32I;
                case 4 -> Format.RGBA32I;
                default -> throw new IllegalStateException("Vertex Attribute size " + size + " is invalid");
            };
            case I64 -> switch (size) {
                case 1 -> Format.R64I;
                case 2 -> Format.RG64I;
                case 3 -> Format.RGB64I;
                case 4 -> Format.RGBA64I;
                default -> throw new IllegalStateException("Vertex Attribute size " + size + " is invalid");
            };

            // --- UNSIGNED INT ---
            case U8 -> switch (size) {
                case 1 -> Format.R8UI;
                case 2 -> Format.RG8UI;
                case 3 -> Format.RGB8UI;
                case 4 -> Format.RGBA8UI;
                default -> throw new IllegalStateException("Vertex Attribute size " + size + " is invalid");
            };
            case U16 -> switch (size) {
                case 1 -> Format.R16UI;
                case 2 -> Format.RG16UI;
                case 3 -> Format.RGB16UI;
                case 4 -> Format.RGBA16UI;
                default -> throw new IllegalStateException("Vertex Attribute size " + size + " is invalid");
            };
            case U32 -> switch (size) {
                case 1 -> Format.R32UI;
                case 2 -> Format.RG32UI;
                case 3 -> Format.RGB32UI;
                case 4 -> Format.RGBA32UI;
                default -> throw new IllegalStateException("Vertex Attribute size " + size + " is invalid");
            };
            case U64 -> switch (size) {
                case 1 -> Format.R64UI;
                case 2 -> Format.RG64UI;
                case 3 -> Format.RGB64UI;
                case 4 -> Format.RGBA64UI;
                default -> throw new IllegalStateException("Vertex Attribute size " + size + " is invalid");
            };
            default -> throw new IllegalStateException("Vertex Attribute of base type " + type + " is invalid!");
        };
    }

}
