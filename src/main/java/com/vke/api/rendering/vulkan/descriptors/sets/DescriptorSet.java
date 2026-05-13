package com.vke.api.rendering.vulkan.descriptors.sets;

import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.*;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorsInfo;
import com.vke.api.rendering.vulkan.descriptors.types.ArrayType;
import com.vke.api.rendering.vulkan.descriptors.types.PrimitiveType;
import com.vke.api.rendering.vulkan.descriptors.types.StructType;
import com.vke.api.rendering.vulkan.descriptors.types.TypeLayout;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.buffers.MappedBuffer;
import com.vke.core.vulkan.device.VulkanRenderDevice;

import java.util.HashMap;

public class DescriptorSet {

    public final HashMap<String, DescriptorBinding> bindings = new HashMap<>();
    public final int set;
    public final long handle;
    public VulkanRenderDevice device;
    public VKEngine engine;

    public DescriptorSet(long handle, VulkanRenderDevice device, VKEngine engine, DescriptorSetLayout layout, DescriptorsInfo additionalInfo) {
        this.set = layout.set;
        this.handle = handle;
        this.device = device;
        this.engine = engine;

        // traverse the layout to set dynamic values and runtime size arrays
        layout.bindings.forEach(bindingLayout -> {
            if (additionalInfo.dynamicBuffers.contains(bindingLayout.name)) bindingLayout.isDynamic = true;

            // Nevermind this is so dogshit it STILL sets the size to 0
//            // TODO: This does NOT work (it sets size to 0), see the check i added
//            TypeLayout tl = bindingLayout.typeLayout;
//            if (tl != null) {
//                resolveRuntimeArraySizes(tl, additionalInfo.runtimeSizeArraySizes);
//                long newSizeMaybe = recomputeSize(tl);
//                // This is the check I added (incase you couldn't tell)
//                if (newSizeMaybe > 0 && newSizeMaybe != tl.size) {
//                    tl.size = newSizeMaybe;
//                }
//            }

            DescriptorBinding binding = createDescriptorBinding(bindingLayout);
            bindings.put(bindingLayout.name, binding);
        });
    }

    public static void resolveRuntimeArraySizes(TypeLayout root, HashMap<String, Integer> runtimeSizes) {
        resolveRecursive(root, root.name, runtimeSizes);
    }

    private static void resolveRecursive(TypeLayout type, String currentPath, HashMap<String, Integer> runtimeSizes) {
        if (type instanceof StructType structType) {

            for (StructType.Member member : structType.members.values()) {

                String memberPath = currentPath + "." + member.name;

                resolveRecursive(member.type, memberPath, runtimeSizes);
            }
        }
        else if (type instanceof ArrayType arrayType) {
            if (arrayType.length == -1) {

                Integer resolvedSize = runtimeSizes.get(currentPath);

                if (resolvedSize != null) {
                    arrayType.length = resolvedSize;
                    arrayType.size = resolvedSize * arrayType.stride;
                } else {
                    throw new IllegalStateException("Missing runtime size for array: " + currentPath);
                }
            }
            resolveRecursive(arrayType.elementType, currentPath, runtimeSizes);
        }
    }

    public static long recomputeSize(TypeLayout type) {
        if (type instanceof PrimitiveType) {
            return type.size;
        }

        if (type instanceof ArrayType arr) {
            arr.size = arr.length * arr.stride;
            return arr.size;
        }

        if (type instanceof StructType struct) {
            long max = 0;
            for (StructType.Member m : struct.members.values()) {
                long memberEnd = m.offset + recomputeSize(m.type);
                max = Math.max(max, memberEnd);
            }
            struct.size = max;
            return max;
        }

        return 0;
    }

    public long getHandle() { return this.handle; }

    public DescriptorBinding createDescriptorBinding(BindingLayout layout) {
        return switch (layout.type) {
            case UNIFORM_BUFFER, STORAGE_BUFFER, UNIFORM_BUFFER_DYNAMIC, STORAGE_BUFFER_DYNAMIC -> {
                BufferUsage usage = (layout.type == DescriptorType.UNIFORM_BUFFER || layout.type == DescriptorType.UNIFORM_BUFFER_DYNAMIC) ? BufferUsage.Bits.UBO.into() : BufferUsage.Bits.SSBO.into();
                MappedBuffer buffer = new MappedBuffer(engine, device, layout.typeLayout.size * layout.descriptorCount, usage);
                yield new BufferBinding(layout, buffer, layout.typeLayout.size, layout.packingType);
            }
            case COMBINED_IMAGE_SAMPLER -> new CombinedImageSamplerBinding(layout);
            case SAMPLED_IMAGE -> new SampledImageBinding(layout);
            case STORAGE_IMAGE -> new StorageImageBinding(layout);
            case SAMPLER -> new SamplerBinding(layout);
            case ACCELERATION_STRUCTURE -> throw new UnsupportedOperationException("Acceleration structures not implemented!"); // TODO: implement this
        };
    }

}
