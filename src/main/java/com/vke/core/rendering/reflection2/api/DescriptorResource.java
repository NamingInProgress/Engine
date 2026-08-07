package com.vke.core.rendering.reflection2.api;

import com.vke.api.rendering.vulkan.descriptors.types.StructType;
import com.vke.api.rendering.vulkan.pipeline.BaseType;
import com.vke.core.assets.pipeline.protocols.shader.ShaderPreprocessor;
import com.vke.core.rendering.reflection2.SpirvItem;
import com.vke.core.rendering.reflection2.SpirvUtils;
import com.vke.utils.iter.helpers.Option;

import java.util.Arrays;

public class DescriptorResource {
    public String name;
    public int set, binding;

    public int nArrayDim = 0;
    public int[] arrayDim = new int[0];
    public int arrayStride;

    public StructType struct;
    public BaseType baseType;
    public int multiWrite;


    public DescriptorResource(SpirvItem item, ShaderPreprocessor.ShaderMetadata meta) {
        this.name = item.name;
        this.set = item.set;
        this.binding = item.binding;
        SpirvItem node = item;
        SpirvItem actualType = item;

        while (node.type == BaseType.TypePointer || node.type == BaseType.Unknown) {
            node = node.componentType;
            actualType = node;
        }
        while (node.type == BaseType.Array && !node.rootOrIsVec) {
            nArrayDim += 1;
            arrayDim = Arrays.copyOf(arrayDim, nArrayDim);
            arrayDim[nArrayDim - 1] = (int) node.scalarBits;
            node = node.componentType;
        }
        this.baseType = actualType.type;
        this.struct = SpirvUtils.createStructType(node);
        this.arrayStride = item.arrayStride;
        this.multiWrite = meta.multipleWrites().getOrDefault(item.name, 1);
        System.out.println();
    }
}
