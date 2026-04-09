package com.vke.api.rendering.vulkan.descriptors;

public enum BaseType {
    FLOAT,
    INT,
    UINT,
    BOOL,
    DOUBLE;

    public static BaseType fromPipelineBaseType(com.vke.api.rendering.vulkan.pipeline.BaseType pipelineBaseType) {
        return switch (pipelineBaseType) {
            case F16, F32 -> FLOAT;
            case I8, I16, I32, I64 -> INT;
            case U8, U16, U32, U64 -> UINT;
            case Boolean -> BOOL;
            case F64 -> DOUBLE;
            default -> throw new RuntimeException("GASP ALARM! (this means Spvc fucked itself in the ass so hard it died)");
        };
    }
}
