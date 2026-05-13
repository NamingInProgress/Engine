package com.vke.api.rendering.vulkan.pipeline;

import com.vke.api.rendering.abstraction.enums.texture.Format;
import org.lwjgl.util.spvc.Spvc;

public enum BaseType {
    Void(0),
    Boolean(1),
    F16(2),
    F32(4),
    F64(8),
    I8(1),
    I16(2),
    I32(4),
    I64(8),
    U8(1),
    U16(2),
    U32(4),
    U64(8),
    Struct(0),
    Image(0),
    Sampler(0),
    SampledImage(0),
    AccelerationStructure(0),
    AtomicCounter(0),
    Unknown(0);

    private final int byteSize;

    BaseType(int i) {
        this.byteSize = i;
    }

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

    public long byteSize() {
        return byteSize;
    }
}
