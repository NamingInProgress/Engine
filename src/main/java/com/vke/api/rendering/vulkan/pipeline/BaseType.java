package com.vke.api.rendering.vulkan.pipeline;

import org.lwjgl.util.spvc.Spvc;

public enum BaseType {
    Void,
    Boolean,
    F16,
    F32,
    F64,
    I8,
    I16,
    I32,
    I64,
    U8,
    U16,
    U32,
    U64,
    Struct,
    Image,
    Sampler,
    SampledImage,
    AccelerationStructure,
    AtomicCounter,
    TypePointer,
    Unknown, Array;

    public static BaseType fromSpvc(int baseType) {
        return switch (baseType) {
            case Spvc.SPVC_BASETYPE_VOID -> Void;
            case Spvc.SPVC_BASETYPE_BOOLEAN -> Boolean;
            case Spvc.SPVC_BASETYPE_FP16 -> F16;
            case Spvc.SPVC_BASETYPE_FP32 -> F32;
            case Spvc.SPVC_BASETYPE_FP64 -> F64;
            case Spvc.SPVC_BASETYPE_INT8 -> I8;
            case Spvc.SPVC_BASETYPE_INT16 -> I16;
            case Spvc.SPVC_BASETYPE_INT32 -> I32;
            case Spvc.SPVC_BASETYPE_INT64 -> I64;
            case Spvc.SPVC_BASETYPE_UINT8 -> U8;
            case Spvc.SPVC_BASETYPE_UINT16 -> U16;
            case Spvc.SPVC_BASETYPE_UINT32 -> U32;
            case Spvc.SPVC_BASETYPE_UINT64 -> U64;
            case Spvc.SPVC_BASETYPE_STRUCT -> Struct;
            case Spvc.SPVC_BASETYPE_IMAGE -> Image;
            case Spvc.SPVC_BASETYPE_SAMPLER -> Sampler;
            case Spvc.SPVC_BASETYPE_SAMPLED_IMAGE -> SampledImage;
            case Spvc.SPVC_BASETYPE_ACCELERATION_STRUCTURE -> AccelerationStructure;
            case Spvc.SPVC_BASETYPE_ATOMIC_COUNTER -> AtomicCounter;
            default -> Unknown;
        };
    }

    public static BaseType forInt(int width, int signedness) {
        if (width == 8) return signedness == 1 ? BaseType.I8 : BaseType.U8;
        if (width == 16) return signedness == 1 ? BaseType.I16 : BaseType.U16;
        if (width == 32) return signedness == 1 ? BaseType.I32 : BaseType.U32;
        if (width == 64) return signedness == 1 ? BaseType.I64 : BaseType.U64;
        return BaseType.Unknown;
    }

    public static BaseType forFloat(int width, int enc) {
        if (width == 16) return BaseType.F16;
        if (width == 32) return BaseType.F32;
        if (width == 64) return BaseType.F64;
        return BaseType.Unknown;
    }
}
