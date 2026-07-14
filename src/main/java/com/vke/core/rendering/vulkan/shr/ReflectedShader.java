package com.vke.core.rendering.vulkan.shr;

import com.vke.api.rendering.abstraction.renderer.enums.ShaderType;
import com.vke.api.rendering.vulkan.descriptors.PrimitiveBaseType;
import com.vke.api.rendering.vulkan.pipeline.BaseType;
import com.vke.api.rendering.vulkan.descriptors.types.*;
import com.vke.core.assets.pipeline.protocols.shader.ShaderPreprocessor;
import com.vke.core.logger.LoggerFactory;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.spvc.Spv;
import org.lwjgl.util.spvc.Spvc;
import org.lwjgl.util.spvc.SpvcReflectedResource;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class ReflectedShader implements Disposable {

    private final ByteBuffer spv;
    private final long context;
    private final long compiler;
    private final ShaderType shaderType;
    private final ShaderPreprocessor.ShaderMetadata metadata;
    public final Identifier shaderPath;

    private final HashMap<ResourceType, ArrayList<? extends Resource>> resources = new HashMap<>();

    public ReflectedShader(Identifier shaderPath, long context, ByteBuffer spirv, ShaderType shaderType, ShaderPreprocessor.ShaderMetadata metadata) {
        this.spv = spirv;
        this.context = context;
        this.shaderType = shaderType;
        this.metadata = metadata;
        this.shaderPath = shaderPath;

        IntBuffer iSpirv = spirv.asIntBuffer();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pIR = stack.mallocPointer(1);
            Spvc.spvc_context_parse_spirv(context, iSpirv, iSpirv.remaining(), pIR);

            PointerBuffer pCompiler = stack.mallocPointer(1);
            Spvc.spvc_context_create_compiler(context, Spvc.SPVC_BACKEND_NONE, pIR.get(0),
                    Spvc.SPVC_CAPTURE_MODE_TAKE_OWNERSHIP, pCompiler);

            this.compiler = pCompiler.get(0);
        }
    }

    public ShaderType getShaderType() {
        return shaderType;
    }

    public ShaderPreprocessor.ShaderMetadata getMetadata() {
        return metadata;
    }

    @SuppressWarnings("unchecked")
    public <T extends Resource> ArrayList<T> getResource(ResourceType type) {
        return (ArrayList<T>) resources.get(type);
    }

    public HashMap<ResourceType, ArrayList<DescriptorResource>> getDescriptors() {
        HashMap<ResourceType, ArrayList<DescriptorResource>> map = new HashMap<>();
        List.of(ResourceType.UBO,
                ResourceType.SSBO,
                ResourceType.SAMPLED_IMAGE,
                ResourceType.STORAGE_IMAGE,
                ResourceType.SEPARATE_IMAGE,
                ResourceType.SEPARATE_SAMPLER).forEach(resourceType -> map.put(resourceType, getDescriptorsForType(resourceType)));

        return map;
    }

    public PushConstantsResource getPushConstants() {
        if (resources.containsKey(ResourceType.PUSH_CONSTANT)) {
            try {
                return (PushConstantsResource) getResource(ResourceType.PUSH_CONSTANT).getFirst();
            } catch (NoSuchElementException e) {
                return null;
            }
        }

        SPVCResource[] resources = getResourcesForType(ResourceType.PUSH_CONSTANT);
        ArrayList<PushConstantsResource> list = new ArrayList<>();

        for (SPVCResource resource : resources) {
            PushConstantsResource res = new PushConstantsResource();

            res.name = resource.name;
            if (resource.baseType == Spvc.SPVC_BASETYPE_STRUCT) {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    PointerBuffer pSize = stack.mallocPointer(1);
                    long typeHandle = Spvc.spvc_compiler_get_type_handle(compiler, resource.typeId);
                    Spvc.spvc_compiler_get_declared_struct_size(compiler, typeHandle, pSize);
                    res.struct = generateStruct(resource, pSize.get(0));
                    res.size = (int) pSize.get(0);
                }
            } else {
                LoggerFactory.get("SPIR-V Reflect").error("Found PushConstants block that is not a struct");
            }

            res.baseTypeRaw = resource.baseType;
            res.baseType = BaseType.fromSpvc(resource.baseType);
            
            list.add(res);
        }

        this.resources.put(ResourceType.PUSH_CONSTANT, list);
        return list.isEmpty() ? null : list.get(0);
    }

    public ArrayList<DescriptorResource> getDescriptorsForType(ResourceType type) {
        if (resources.containsKey(type)) return getResource(type);

        SPVCDescriptorResource[] resources = getDescriptorResources(type);
        ArrayList<DescriptorResource> list = new ArrayList<>();

        for (SPVCDescriptorResource resource : resources) {
            DescriptorResource descriptorResource = new DescriptorResource();

            descriptorResource.set = resource.set;
            descriptorResource.binding = resource.binding;
            descriptorResource.name = resource.name;
            if (resource.baseType == Spvc.SPVC_BASETYPE_STRUCT) {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    PointerBuffer pSize = stack.mallocPointer(1);
                    long typeHandle = Spvc.spvc_compiler_get_type_handle(compiler, resource.typeId);
                    Spvc.spvc_compiler_get_declared_struct_size(compiler, typeHandle, pSize);
                    descriptorResource.struct = generateStruct(resource, pSize.get(0));
                    descriptorResource.multiWrite = this.getMetadata().multipleWrites().getOrDefault(resource.name, 1);
                }
            }
            descriptorResource.baseTypeRaw = resource.baseType;
            descriptorResource.baseType = BaseType.fromSpvc(resource.baseType);

            descriptorResource.nArrayDim = Spvc.spvc_type_get_num_array_dimensions(resource.typeHandle);
            descriptorResource.arrayDim = new int[descriptorResource.nArrayDim];
            for (int i = 0; i < descriptorResource.nArrayDim; i++) {
                descriptorResource.arrayDim[i] = Spvc.spvc_type_get_array_dimension(resource.typeHandle, i);
            }
            descriptorResource.arrayStride = resource.arrayStride;
            /**
             *
             * uniform a {
             *      ....
             * } UBO[a][b][c] -> a * b * c * stride = size
             *
             */

            list.add(descriptorResource);
        }

        this.resources.put(type, list);
        return list;
    }

    public ArrayList<VertexAttributeResource> getVAOs() {
        if (resources.containsKey(ResourceType.VAO)) return getResource(ResourceType.VAO);
        SPVCDescriptorResource[] resources = getDescriptorResources(ResourceType.VAO);
        ArrayList<VertexAttributeResource> list = new ArrayList<>();

        for (SPVCDescriptorResource resource : resources) {
            VertexAttributeResource var = new VertexAttributeResource();

            var.location = resource.location;
            var.name = resource.name;

            int bit_width = Spvc.spvc_type_get_bit_width(resource.typeHandle);
            int rows = Spvc.spvc_type_get_vector_size(resource.typeHandle);
            
            var.stride = (bit_width / 8) * rows;
            var.vecSize = rows;
            var.baseType = BaseType.fromSpvc(resource.baseType);

            list.add(var);
        }

        this.resources.put(ResourceType.VAO, list);
        return list;
    }

    private StructType generateStruct(SPVCResource resource, long size) {
        StructType s = new StructType();
        s.name = resource.name;
        s.size = size;

        long typeHandle = Spvc.spvc_compiler_get_type_handle(compiler, resource.typeId);
        int memberCount = Spvc.spvc_type_get_num_member_types(typeHandle);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DiscoverableMember[] members = new DiscoverableMember[memberCount];
            for (int i = 0; i < memberCount; i++) {
                DiscoverableMember member = new DiscoverableMember();
                member.parentBaseTypeId = resource.baseTypeId;
                member.idx = i;

                member.name = Spvc.spvc_compiler_get_member_name(compiler, resource.baseTypeId, i);
                member.id = Spvc.spvc_type_get_member_type(typeHandle, i);

                IntBuffer buf = stack.mallocInt(1);
                PointerBuffer pSize = stack.mallocPointer(1);
                Spvc.spvc_compiler_type_struct_member_offset(compiler, typeHandle, i, buf);
                Spvc.spvc_compiler_get_declared_struct_member_size(compiler, typeHandle, i, pSize);

                member.offset = buf.get(0);
                member.size = pSize.get(0);

                members[i] = member;
            }

            for (int i = 0; i < memberCount; i++) {
                long expectedSize;
                if (i + 1 < memberCount) {
                    expectedSize = members[i + 1].offset - members[i].offset;
                } else {
                    expectedSize = size - members[i].offset;
                }

                discoverMemberWithPotentiallyComplexType(members[i], expectedSize);
                s.members.put(members[i].name, populateMember(members[i]));
            }
        }

        return s;
    }

    public StructType.Member populateMember(DiscoverableMember discoverableMember) {
        BaseType type = BaseType.fromSpvc(discoverableMember.baseType);

        TypeLayout baseTypeLayout;

        if (discoverableMember.isPointer) {
            baseTypeLayout = new PointerType();
        } else if (discoverableMember.matrixRows > 1 && discoverableMember.matrixColumns > 1) {
            baseTypeLayout = new MatrixType(discoverableMember.matrixRows, discoverableMember.matrixColumns,
                    discoverableMember.matrixStride, PrimitiveBaseType.fromPipelineBaseType(type));
        } else if (discoverableMember.struct != null) {
            baseTypeLayout = discoverableMember.struct;
        } else {
            baseTypeLayout = new PrimitiveType();
            ((PrimitiveType) baseTypeLayout).vecSize = discoverableMember.matrixRows > 1 ? discoverableMember.matrixRows : (Math.max(discoverableMember.matrixColumns, 1));
            ((PrimitiveType) baseTypeLayout).scalarType = PrimitiveBaseType.fromPipelineBaseType(type);
        }

        if (discoverableMember.nArrayDim > 1) {
            baseTypeLayout = compactArray(discoverableMember.arrayDim, discoverableMember.arrayStride, baseTypeLayout);
        } else if (discoverableMember.nArrayDim == 1) {
            if ((discoverableMember.arrayDim[0] > 1) || (discoverableMember.arrayDim[0] == 0)) {
                baseTypeLayout = compactArray(discoverableMember.arrayDim, discoverableMember.arrayStride, baseTypeLayout);
            }
        }

        baseTypeLayout.size = discoverableMember.size;
        baseTypeLayout.name = discoverableMember.name;

        return new StructType.Member(discoverableMember.name, discoverableMember.offset, discoverableMember.size, baseTypeLayout);
    }

    public ArrayType compactArray(int[] arrayDim, long arrayStride, TypeLayout elementType) {
        ArrayType result = new ArrayType();

        int length = 1;
        for (int d : arrayDim) {
            if (d == 0) { // runtime-size array
                length = 0;
                break;
            }
            length *= d;
        }

        result.elementCount = length;
        result.stride = arrayStride;
        result.elementType = elementType;
        result.size = arrayStride * length;

        return result;
    }

    public static class DiscoverableMember {
        public int parentBaseTypeId;
        public int idx;

        public long offset;
        public long size;

        // Common Data
        public String name;
        public int id;
        public long typeHandle;
        public int baseType;
        public int baseTypeId;
        public boolean isPointer;

        // Array Data
        public int nArrayDim;
        public int[] arrayDim;
        public int arrayStride;

        // Vec/Matrix Data
        public int matrixRows;
        public int matrixColumns;
        public int matrixStride;

        // Struct Data
        public StructType struct;
    }

    private void discoverMemberWithPotentiallyComplexType(DiscoverableMember member, long expectedSize) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            member.typeHandle = Spvc.spvc_compiler_get_type_handle(compiler, member.id);
            member.baseType = Spvc.spvc_type_get_basetype(member.typeHandle);
            member.baseTypeId = Spvc.spvc_type_get_base_type_id(member.typeHandle);
            member.nArrayDim = Spvc.spvc_type_get_num_array_dimensions(member.typeHandle);
            member.arrayDim = new int[member.nArrayDim];
            for (int i = 0; i < member.nArrayDim; i++) {
                member.arrayDim[i] = Spvc.spvc_type_get_array_dimension(member.typeHandle, i);
            }
            member.matrixRows = Spvc.spvc_type_get_vector_size(member.typeHandle);
            member.matrixColumns = Spvc.spvc_type_get_columns(member.typeHandle);

            IntBuffer buf = stack.callocInt(1);

            if (member.matrixRows > 1 || member.matrixColumns > 1) {
                Spvc.spvc_compiler_type_struct_member_matrix_stride(compiler, member.typeHandle, member.idx, buf);
                member.matrixStride = buf.get(0);
            }

            if (member.nArrayDim != 0) {
                Spvc.spvc_compiler_type_struct_member_array_stride(compiler, member.typeHandle, member.idx, buf);
                member.arrayStride = buf.get(0);
            }
            if (expectedSize > member.size) {
                member.size = expectedSize;
            }
            member.isPointer = Spvc.spvc_type_get_storage_class(member.typeHandle) == Spv.SpvStorageClassPhysicalStorageBufferEXT;
            if (!member.isPointer && member.baseType == Spvc.SPVC_BASETYPE_STRUCT) {
                SPVCResource resource = new SPVCResource(member.name, member.id, member.baseType, member.baseTypeId, member.baseType, member.typeHandle);
                member.struct = generateStruct(resource, member.size);
            }
        }
    }

    private void discoverChildlessSadOrphanMemberWithPotentiallyComplexType(DiscoverableMember member) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            member.typeHandle = Spvc.spvc_compiler_get_type_handle(compiler, member.id);
            member.baseType = Spvc.spvc_type_get_basetype(member.typeHandle);
            member.baseTypeId = Spvc.spvc_type_get_base_type_id(member.typeHandle);
            PointerBuffer pSize = stack.mallocPointer(1);
            // TODO: WARNING GASP ALARM
            // TODO: This needs potentially to change what id it uses, refer to ShaderReflectionTest (rn its 2am and its not compiling)
            Spvc.spvc_compiler_get_declared_struct_size(compiler, member.typeHandle, pSize);
            member.size = pSize.get(0);
            member.nArrayDim = Spvc.spvc_type_get_num_array_dimensions(member.typeHandle);
            member.arrayDim = new int[member.nArrayDim];
            for (int i = 0; i < member.nArrayDim; i++) {
                member.arrayDim[i] = Spvc.spvc_type_get_array_dimension(member.typeHandle, i);
            }
            member.matrixRows = Spvc.spvc_type_get_vector_size(member.typeHandle);
            member.matrixColumns = Spvc.spvc_type_get_columns(member.typeHandle);
            if (member.baseType == Spvc.SPVC_BASETYPE_STRUCT) {
                SPVCResource resource = new SPVCResource(member.name, member.id, member.baseType, member.baseTypeId, member.baseType, member.typeHandle);
                member.struct = generateStruct(resource, member.size);
            }
        }
    }

    private SPVCDescriptorResource[] getDescriptorResources(ResourceType type) {
        SPVCResource[] resources = getResourcesForType(type);
        SPVCDescriptorResource[] descriptors = new SPVCDescriptorResource[resources.length];

        for (int i = 0; i < resources.length; i++) {
            SPVCResource resource = resources[i];

            int set = Spvc.spvc_compiler_get_decoration(compiler, resource.id, Spv.SpvDecorationDescriptorSet);
            int binding = Spvc.spvc_compiler_get_decoration(compiler, resource.id, Spv.SpvDecorationBinding);
            int location = Spvc.spvc_compiler_get_decoration(compiler, resource.id, Spv.SpvDecorationLocation);
            int arrayStride = Spvc.spvc_compiler_get_decoration(compiler, resource.id, Spv.SpvDecorationArrayStride);
            int matrixStride = Spvc.spvc_compiler_get_decoration(compiler, resource.id, Spv.SpvDecorationMatrixStride);

            descriptors[i] = new SPVCDescriptorResource(
                    resource.name,
                    resource.id,
                    resource.baseTypeId,
                    resource.typeId,
                    resource.baseType,
                    resource.typeHandle,
                    set,
                    binding,
                    location,
                    arrayStride,
                    matrixStride
            );
        }

        return descriptors;
    }

    private SPVCResource[] getResourcesForType(ResourceType type) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pResources = stack.mallocPointer(1);
            Spvc.spvc_compiler_create_shader_resources(compiler, pResources);
            long resources = pResources.get(0);

            PointerBuffer pResourceList = stack.mallocPointer(1);
            PointerBuffer pResourceCount = stack.mallocPointer(1);

            Spvc.spvc_resources_get_resource_list_for_type(resources, type.getSpvc(), pResourceList, pResourceCount);

            int resourceCount = (int) pResourceCount.get(0);
            SPVCResource[] arr = new SPVCResource[resourceCount];

            if (resourceCount == 0) return arr;

            SpvcReflectedResource.Buffer resourcesBuffer = SpvcReflectedResource.create(pResourceList.get(0), resourceCount);
            int i = 0;
            for (SpvcReflectedResource res : resourcesBuffer) {
                long typeHandle = Spvc.spvc_compiler_get_type_handle(compiler, res.type_id());
                int baseType = Spvc.spvc_type_get_basetype(typeHandle);
                arr[i++] = new SPVCResource(Spvc.spvc_compiler_get_name(compiler, res.id()), res.id(), res.base_type_id(), res.type_id(), baseType, typeHandle);
            }

            return arr;
        }
    }

    @Override
    public void free() {

    }

    public static class SPVCResource {
        public String name;
        public int id;
        public int baseTypeId;
        public int typeId;
        public int baseType;
        public long typeHandle;

        public SPVCResource(String name, int id, int baseTypeId, int typeId, int baseType, long typeHandle) {
            this.name = name;
            this.id = id;
            this.baseTypeId = baseTypeId;
            this.typeId = typeId;
            this.baseType = baseType;
            this.typeHandle = typeHandle;
        }
    }

    public static class SPVCDescriptorResource extends SPVCResource {

        public int set, binding;
        public int location;
        public int arrayStride, matrixStride;

        public SPVCDescriptorResource(String name, int id, int baseTypeId, int typeId, int baseType, long typeHandle, int set, int binding, int location, int arrayStride, int matrixStride) {
            super(name, id, baseTypeId, typeId, baseType, typeHandle);
            this.set = set;
            this.binding = binding;
            this.location = location;
            this.arrayStride = arrayStride;
            this.matrixStride = matrixStride;
        }

    }

    public abstract static class Resource {

        public String name;

    }

    public static class DescriptorResource extends Resource {

        public int set, binding;

        public int nArrayDim;
        public int[] arrayDim;
        public int arrayStride;

        public StructType struct;
        public int baseTypeRaw;
        public BaseType baseType;
        public int multiWrite;

    }

    public static class PushConstantsResource extends Resource {

        public StructType struct;
        public int baseTypeRaw;
        public BaseType baseType;
        public int size;

    }

    public static class VertexAttributeResource extends Resource {
        public int location;
        public int stride;
        public BaseType baseType;
        public int vecSize;
    }

    public enum ResourceType {

        UNKNOWN(Spvc.SPVC_RESOURCE_TYPE_UNKNOWN),
        UBO(Spvc.SPVC_RESOURCE_TYPE_UNIFORM_BUFFER),
        SSBO(Spvc.SPVC_RESOURCE_TYPE_STORAGE_BUFFER),
        VAO(Spvc.SPVC_RESOURCE_TYPE_STAGE_INPUT),
        STORAGE_IMAGE(Spvc.SPVC_RESOURCE_TYPE_STORAGE_IMAGE),
        SAMPLED_IMAGE(Spvc.SPVC_RESOURCE_TYPE_SAMPLED_IMAGE),
        ATOMIC_COUNTER(Spvc.SPVC_RESOURCE_TYPE_ATOMIC_COUNTER),
        PUSH_CONSTANT(Spvc.SPVC_RESOURCE_TYPE_PUSH_CONSTANT),
        SEPARATE_IMAGE(Spvc.SPVC_RESOURCE_TYPE_SEPARATE_IMAGE),
        SEPARATE_SAMPLER(Spvc.SPVC_RESOURCE_TYPE_SEPARATE_SAMPLERS),
        ACCELERATION_STRUCTURE(Spvc.SPVC_RESOURCE_TYPE_ACCELERATION_STRUCTURE),
        RAY_QUERY(Spvc.SPVC_RESOURCE_TYPE_RAY_QUERY),
        RECORD_BUFFER(Spvc.SPVC_RESOURCE_TYPE_SHADER_RECORD_BUFFER);

        private final int spvc;

        ResourceType(int spvc) {
            this.spvc = spvc;
        }

        public int getSpvc() { return this.spvc; }

        public boolean isDescriptorBuffer() { return this == UBO || this == SSBO; }

    }

}
