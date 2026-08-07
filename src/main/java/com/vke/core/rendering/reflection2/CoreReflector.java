package com.vke.core.rendering.reflection2;

import com.carrotsearch.hppc.ByteArrayList;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.api.rendering.vulkan.pipeline.BaseType;
import com.vke.core.assets.pipeline.protocols.shader.ShaderPreprocessor;
import com.vke.core.file.spirv.SpirvDecoder;
import com.vke.core.file.spirv.SpirvInstruction;
import com.vke.core.rendering.reflection2.api.DescriptorCategory;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CoreReflector {
    private final SpirvDecoder decoder;

    private final IntObjectHashMap<SpirvItem> ids;

    public CoreReflector(InputStream input) {
        this.decoder = new SpirvDecoder(input);
        this.ids = new IntObjectHashMap<>();
    }

    public CoreReflectedShader reflect(Identifier ident, ShaderType shaderType, ShaderPreprocessor.ShaderMetadata metadata) {
        Iter<SpirvInstruction> instructions = decoder.instructions();

        boolean inFn = false;

        for (SpirvInstruction instruction : instructions) {
            int op = instruction.opcode();
            int[] ops = instruction.operands();

            if (inFn) {
                if (op == Op.FUNCTION_END) {
                    inFn = false;
                }
                continue;
            }

            if (op == Op.FUNCTION) {
                inFn = true;
                continue;
            }

            if (op == Op.VARIABLE) {
                int resType = ops[0];
                int res = ops[1];
                SpirvItem variable = getId(res);
                SpirvItem actualType = getId(resType).componentType;
                int storageClass = ops[2];
                DescriptorCategory category = DescriptorCategory.fromStorageClass(storageClass);
                variable.componentType = actualType;
                variable.type = BaseType.TypePointer;
                variable.category = category;
            }

            if (isType(op)) {
                handleType(op, ops);
            }

            if (op == Op.NAME) {
                int res = ops[0];
                String name = retrieveString(ops, 1);
                getId(res).name = name;
            }

            if (op == Op.MEMBER_NAME) {
                int res = ops[0];
                int member = ops[1];
                String name = retrieveString(ops, 2);
                getMember(getId(res), member).name = name;
            }

            if (op == Op.DECORATE) {
                applyDecoration(getId(ops[0]), ops[1], ops, 2);
            }

            if (op == Op.DECORATE_ID) {
                applyMemberDecoration(getMember(getId(ops[0]), ops[1]), ops[2], ops, 3);
            }

            if (op == Op.CONSTANT) {
                int resType = ops[0];
                int res = ops[1];
                long value = ops[2];
                if (ops.length == 4) {
                    long upper = ops[3];
                    value |= (upper << 32);
                }
                SpirvItem typeType = getId(resType);
                SpirvItem type = getId(res);
                type.scalarBits = value;
                type.type = typeType.type;
            }
        }

        var pushConstants = Iter.of(ids.values())
                .map(t -> t.value)
                .first(t -> t.category == DescriptorCategory.PUSH_CONSTANT)
                .unwrapOrNull();

        var descriptors = Iter.of(ids.values()).filterMap(t -> {
            if (t.value.set >= 0 && t.value.binding >= 0) {
                return Option.some(t.value);
            } return Option.none();
        }).collectToList();

        var vaos = Iter.of(ids.values()).filterMap(t -> {
            if (t.value.location >= 0) {
                return Option.some(t.value);
            } return Option.none();
        }).collectToList();

        System.out.println(descriptors);

        return new CoreReflectedShader(ident, shaderType, metadata, pushConstants, descriptors, vaos);
    }

    private boolean isType(int op) {
        return op >= 19 && op <= 39;
    }

    private boolean isDecoration(int op) {
        return op >= 71 && op <= 75;
    }

    private void handleType(int op, int[] ops) {
        switch (op) {
            case Op.TYPE_BOOL -> newPrimType(BaseType.Boolean, ops[0]);
            case Op.TYPE_INT -> {
                int res = ops[0];
                int width = ops[1];
                int sign = ops[2];
                newPrimType(BaseType.forInt(width, sign), res);
            }
            case Op.TYPE_FLOAT -> {
                int res = ops[0];
                int width = ops[1];
                int enc = -1;
                if (ops.length == 3) enc = ops[2];
                newPrimType(BaseType.forFloat(width, enc), res);
            }
            case Op.TYPE_VECTOR -> {
                int res = ops[0];
                SpirvItem componentType = getId(ops[1]);
                int componentCount = ops[2];

                SpirvItem vector = getId(res);
                vector.componentType = componentType;
                vector.scalarBits = componentCount;
                vector.type = BaseType.Array;
            }
            case Op.TYPE_MATRIX -> {
                int res = ops[0];
                SpirvItem columnType = getId(ops[1]);
                int colCount = ops[2];

                SpirvItem matrix = getId(res);
                matrix.componentType = columnType;
                matrix.scalarBits = colCount;
                matrix.type = BaseType.Matrix;
            }
            case Op.TYPE_IMAGE -> newPrimType(BaseType.Image, ops[0]);
            case Op.TYPE_SAMPLER -> newPrimType(BaseType.Sampler, ops[0]);
            case Op.TYPE_SAMPLED_IMAGE -> newPrimType(BaseType.SampledImage, ops[0]);
            case Op.TYPE_POINTER -> {
                int res = ops[0];
                int storageClass = ops[1];
                DescriptorCategory category = DescriptorCategory.fromStorageClass(storageClass);
                SpirvItem type = getId(res);
                type.componentType = getId(ops[2]);
                type.category = category;
            }

            case Op.TYPE_STRUCT ->  {
                int res = ops[0];
                SpirvItem struct = getId(res);
                struct.type = BaseType.Struct;
                for (int i = 1; i < ops.length; i++) {
                    getMember(struct, i - 1).type = getId(ops[i]);
                }
            }
            case Op.TYPE_OPAQUE -> {
                int res = ops[0];
                String name = retrieveString(ops, 1);
                SpirvItem struct = getId(res);
                struct.name = name;
                struct.type = BaseType.Struct;
            }
            case Op.TYPE_ARRAY -> {
                int res = ops[0];
                int elemType = ops[1];
                long length = getId(ops[2]).scalarBits;

                SpirvItem type = getId(res);
                type.type = BaseType.Array;
                type.scalarBits = length;
                type.componentType = getId(elemType);
                System.out.println();
            }
            case Op.TYPE_RUNTIME_ARRAY -> {
                int res = ops[0];
                int elemType = ops[1];

                SpirvItem type = getId(res);
                type.type = BaseType.Array;
                type.scalarBits = -1;
                type.componentType = getId(elemType);
            }
            case Op.TYPE_FORWARD_POINTER -> {

            }
            default -> System.out.println("GASP ALARM!" + op);
        }
    }

    private void applyDecoration(SpirvItem type, int decoration, int[] ops, int off) {
        switch (decoration) {
            case Decoration.BLOCK -> type.block = true;
            case Decoration.ROW_MAJOR -> type.rowMajor = true;
            case Decoration.COL_MAJOR -> type.rowMajor = false;
            case Decoration.ARRAY_STRIDE -> type.arrayStride = ops[off];
            case Decoration.MATRIX_STRIDE -> type.matrixStride = ops[off];
            case Decoration.LOCATION -> type.location = ops[off];
            case Decoration.BINDING -> type.binding = ops[off];
            case Decoration.DESCRIPTOR_SET -> type.set = ops[off];
        }
    }

    private void applyMemberDecoration(SpirvItem.Member member, int decoration, int[] ops, int off) {
        if (decoration == Decoration.OFFSET) {
            member.offset = ops[off];
        }
    }

    private String retrieveString(int[] ops, int off) {
        //educated guess
        ByteArrayList bytes = new ByteArrayList((ops.length - off) * 3);
        for (int i = off; i < ops.length; i++) {
            int op = ops[i];
            byte a = (byte) (op & 0xFF);
            byte b = (byte) ((op >>> 8) & 0xFF);
            byte c = (byte) ((op >>> 16) & 0xFF);
            byte d = (byte) ((op >>> 24) & 0xFF);

            if (a == '\0') break;
            bytes.add(a);

            if (b == '\0') break;
            bytes.add(b);

            if (c == '\0') break;
            bytes.add(c);

            if (d == '\0') break;
            bytes.add(d);
        }
        return new String(bytes.toArray(), StandardCharsets.UTF_8);
    }

    private SpirvItem getId(int id) {
        if (ids.containsKey(id)) {
            return ids.get(id);
        }
        SpirvItem n = new SpirvItem();
        ids.put(id, n);
        return n;
    }

    private SpirvItem.Member getMember(SpirvItem parent, int id) {
        if (parent.members.containsKey(id)) {
            return parent.members.get(id);
        }
        SpirvItem.Member m = new SpirvItem.Member();
        parent.members.put(id, m);
        return m;
    }

    private void newPrimType(BaseType baseType, int res) {
        getId(res).type = baseType;
    }
}
