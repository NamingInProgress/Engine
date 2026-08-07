package com.vke.core.rendering.reflection2;

import com.vke.api.rendering.vulkan.pipeline.BaseType;
import com.vke.core.VKEngine;

public class SpirvUtils {
    public static int computeSize(SpirvItem item) {
        if (item == null) return 0;

        if (item.type == BaseType.Unknown || item.type == BaseType.TypePointer) {
            return computeSize(item.componentType);
        }

        int base = switch (item.type) {
            case Void -> 0;
            case Boolean, I8, U8 -> 1;
            case F16, I16, U16 -> 2;
            case F32, I32, U32 -> 4;
            case F64, I64, U64 -> 8;
            case Struct -> {
                int s = 0;
                for (var member : item.members.values()) {
                    s += computeSize(member.value.type);
                }
                yield s;
            }
            case Image, Sampler, SampledImage -> -1;
            case AccelerationStructure, AtomicCounter -> {
                throw new UnsupportedOperationException();
            }
            case Array -> {
                if (item.arrayStride > 0) {
                    yield (int) (item.arrayStride * item.scalarBits);
                } else {
                    SpirvItem component = item.componentType;
                    yield (int) (computeSize(component) * item.scalarBits);
                }
            }
            case Matrix -> {
                if (item.matrixStride > 0) {
                    yield (int) (item.matrixStride * item.scalarBits);
                } else {
                    SpirvItem component = item.componentType;
                    int componentSize = computeSize(component);
                    yield (int) (componentSize * item.scalarBits);
                }
            }
            default -> -1;
        };

        if (item.scalarBits > 0 && base >= 0) {
            return (int) (base * item.scalarBits);
        } else {
            return base;
        }
    }
}
