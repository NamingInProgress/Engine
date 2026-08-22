package com.vke.core.rendering.reflection2;

import com.vke.api.rendering.vulkan.descriptors.PrimitiveBaseType;
import com.vke.api.rendering.vulkan.descriptors.types.*;
import com.vke.api.rendering.vulkan.pipeline.BaseType;
import com.vke.core.rendering.reflection2.api.DescriptorCategory;
import com.vke.utils.iter.Iter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;

public class SpirvUtils {

    public static int computeSize(SpirvItem item) {
        return computeSize(item, false);
    }

    public static int computeSize(SpirvItem item, boolean nested) {
        if (item == null) return 0;

        int size = switch (item.type) {
            case Void -> 0;
            case Unknown -> computeSize(item.componentType, nested);
            case Boolean, I8, U8 -> 1;
            case F16, I16, U16 -> 2;
            case F32, I32, U32 -> 4;
            case F64, I64, U64 -> 8;
            case Struct -> {
                if (item.members.isEmpty()) yield 0;

                SpirvItem.Member last = Iter.of(item.members.values())
                        .map(t -> t.value)
                        .maxBy(Comparator.comparingInt(m -> m.offset));

                yield last.offset + computeSize(last.type, true);
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
                    yield (int) (computeSize(component, true) * item.scalarBits);
                }
            }
            case Matrix -> {
                if (item.matrixStride > 0) {
                    yield (int) (item.matrixStride * item.scalarBits);
                } else {
                    SpirvItem component = item.componentType;
                    int componentSize = computeSize(component, true);
                    yield (int) (componentSize * item.scalarBits);
                }
            }
            case TypePointer -> {
                if (item.category == DescriptorCategory.STORAGE_BUFFER) {
                    yield nested ? 8 : computeSize(item.componentType, true);
                } else {
                    yield computeSize(item.componentType, true);
                }
            }
        };
        if (item.name != null && !item.name.isEmpty()) {
            System.out.println("Computed size " + size + " for " + item.name);
        }
        return size;
    }

    public static StructType createStructType(SpirvItem structItem) {
        if (structItem.type == BaseType.TypePointer || structItem.type == BaseType.Unknown) {
            return createStructType(structItem.componentType);
        }

        if (structItem.type != BaseType.Struct) return null;

        SpirvItem.Member[] members = structItem.members.values().toArray(SpirvItem.Member.class);
        Arrays.sort(members, Comparator.comparingInt(m -> m.offset));

        LinkedHashMap<String, StructType.Member> structMembers = new LinkedHashMap<>(members.length);
        for (SpirvItem.Member member : members) {
            StructType.Member structMember = createStructMember(member);
            structMembers.put(member.name, structMember);
        }

        StructType struct = new StructType();
        struct.size = computeSize(structItem);
        struct.members = structMembers;
        return struct;
    }

    @SuppressWarnings("all")
    private static TypeLayout createTypeLayout(SpirvItem type, String name) {
        TypeLayout baseTypeLayout;

        if (type.type == BaseType.TypePointer && type.category == DescriptorCategory.STORAGE_BUFFER) {
            baseTypeLayout = new PointerType();
        } else if (type.type == BaseType.Matrix) {
            baseTypeLayout = new MatrixType((int) type.scalarBits, (int) type.componentType.scalarBits,
                    type.matrixStride, PrimitiveBaseType.fromPipelineBaseType(type.componentType.componentType.type));
        } else if (type.type == BaseType.Struct || type.type == BaseType.TypePointer || type.type == BaseType.Unknown) {
            baseTypeLayout = createStructType(type);
        } else if (type.type == BaseType.Array && !type.rootOrIsVec) {
            ArrayType arrayTypeLayout = new ArrayType();
            arrayTypeLayout.stride = type.arrayStride;
            arrayTypeLayout.elementCount = (int) type.scalarBits;
            arrayTypeLayout.elementType = createTypeLayout(type.componentType, "");
            baseTypeLayout = arrayTypeLayout;
        } else {
            PrimitiveType primitiveTypeLayout = new PrimitiveType();
            if (type.type == BaseType.Array) {
                primitiveTypeLayout.vecSize = (int) type.scalarBits;
                primitiveTypeLayout.scalarType = PrimitiveBaseType.fromPipelineBaseType(type.componentType.type);
            } else {
                primitiveTypeLayout.scalarType = PrimitiveBaseType.fromPipelineBaseType(type.type);
            }
            baseTypeLayout = primitiveTypeLayout;
        }

        baseTypeLayout.size = computeSize(type);
        baseTypeLayout.name = name;


        return baseTypeLayout;
    }

    public static StructType.Member createStructMember(SpirvItem.Member member) {
        TypeLayout baseTypeLayout = createTypeLayout(member.type, member.name);
        return new StructType.Member(baseTypeLayout.name, member.offset, baseTypeLayout.size, baseTypeLayout);
    }
}
