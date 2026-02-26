package com.vke.core.services.shr;

import com.vke.api.pipeline.Entry;
import com.vke.api.pipeline.Struct;
import com.vke.utils.Disposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.spvc.Spv;
import org.lwjgl.util.spvc.Spvc;
import org.lwjgl.util.spvc.SpvcReflectedResource;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class ReflectedShader implements Disposable {

    private final ByteBuffer spv;
    private final long context;
    private final long compiler;

    private final HashMap<ResourceType, ArrayList<Resource>> resources = new HashMap<>();

    public ReflectedShader(long context, ByteBuffer spirv) {
        this.spv = spirv;
        this.context = context;

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

    @SuppressWarnings("unchecked")
    public <T extends Resource> ArrayList<T> getResource(ResourceType type) {
        return (ArrayList<T>) resources.get(type);
    }

    public ArrayList<BufferDescriptorResource> getUBOs() {
        if (resources.containsKey(ResourceType.UBO)) return getResource(ResourceType.UBO);

        SPVCDescriptorResource[] resources = getDescriptorResources(ResourceType.UBO);
        ArrayList<BufferDescriptorResource> list = new ArrayList<>();

        for (SPVCDescriptorResource resource : resources) {
            BufferDescriptorResource bdr = new BufferDescriptorResource();

            bdr.set = resource.set;
            bdr.binding = resource.binding;
            bdr.name = resource.name;
            if (resource.baseType == Spvc.SPVC_BASETYPE_STRUCT) {
                bdr.struct = generateStruct(resource);
            }
            bdr.baseTypeRaw = resource.baseType;
            bdr.baseType = Entry.BaseType.fromSpvc(resource.baseType);

            bdr.nArrayDim = Spvc.spvc_type_get_num_array_dimensions(resource.baseTypeId);
            bdr.arrayDim = new int[bdr.nArrayDim];
            for (int i = 0; i < bdr.nArrayDim; i++) {
                bdr.arrayDim[i] = Spvc.spvc_type_get_array_dimension(resource.baseTypeId, i);
            }
            bdr.arrayStride = resource.arrayStride;

            list.add(bdr);
        }

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

            DiscoverableMember member = new DiscoverableMember();
            member.name = resource.name;
            member.id = resource.id;
            member.arrayStride = resource.arrayStride;
            member.matrixStride = resource.matrixStride;
            member.offset = Spvc.spvc_compiler_get_decoration(compiler, resource.id, Spv.SpvDecorationOffset);

            var.entry = new Entry(member.name, 0, member.offset);
            discoverChildlessSadOrphanMemberWithPotentiallyComplexType(member);
            var.entry.size = member.size;
            var.entry.digestDiscoverableMember(member);

            list.add(var);
        }

        return list;
    }

    private Struct generateStruct(SPVCResource resource) {
        Struct s = new Struct();

        long typeHandle = Spvc.spvc_compiler_get_type_handle(compiler, resource.baseTypeId);
        int memberCount = Spvc.spvc_type_get_num_member_types(typeHandle);
        try (MemoryStack stack = MemoryStack.stackPush()) {
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

                Entry memberEntry = new Entry(member.name, member.size, member.offset);

                discoverMemberWithPotentiallyComplexType(member);
                memberEntry.digestDiscoverableMember(member);
                s.entries.put(member.name, memberEntry);
            }
        }

        return s;
    }

    public static class DiscoverableMember {
        public int parentBaseTypeId;
        public int idx;

        public int offset;
        public long size;

        // Common Data
        public String name;
        public int id;
        public long typeHandle;
        public int baseType;
        public int baseTypeId;

        // Array Data
        public int nArrayDim;
        public int[] arrayDim;
        public int arrayStride;

        // Vec/Matrix Data
        public int matrixRows;
        public int matrixColumns;
        public int matrixStride;

        // Struct Data
        public Struct struct;
    }

    private void discoverMemberWithPotentiallyComplexType(DiscoverableMember member) {
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
            Spvc.spvc_compiler_type_struct_member_matrix_stride(compiler, member.parentBaseTypeId, member.idx, buf);
            member.matrixStride = buf.get(0);
            Spvc.spvc_compiler_type_struct_member_array_stride(compiler, member.parentBaseTypeId, member.idx, buf);
            member.arrayStride = buf.get(0);
            if (member.baseType == Spvc.SPVC_BASETYPE_STRUCT) {
                SPVCResource resource = new SPVCResource(member.name, member.id, member.baseType, member.baseTypeId, member.baseType);
                member.struct = generateStruct(resource);
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
                SPVCResource resource = new SPVCResource(member.name, member.id, member.baseType, member.baseTypeId, member.baseType);
                member.struct = generateStruct(resource);
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
            SpvcReflectedResource.Buffer resourcesBuffer = SpvcReflectedResource.create(pResourceList.get(0), resourceCount);

            int i = 0;
            for (SpvcReflectedResource res : resourcesBuffer) {
                long typeHandle = Spvc.spvc_compiler_get_type_handle(compiler, res.id());
                int baseType = Spvc.spvc_type_get_basetype(typeHandle);
                arr[i++] = new SPVCResource(res.nameString(), res.id(), res.base_type_id(), res.type_id(), baseType);
            }

            resourcesBuffer.free();

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

        public SPVCResource(String name, int id, int baseTypeId, int typeId, int baseType) {
            this.name = name;
            this.id = id;
            this.baseTypeId = baseTypeId;
            this.typeId = typeId;
            this.baseType = baseType;
        }
    }

    public static class SPVCDescriptorResource extends SPVCResource {

        public int set, binding;
        public int location;
        public int arrayStride, matrixStride;

        public SPVCDescriptorResource(String name, int id, int baseTypeId, int typeId, int baseType, int set, int binding, int location, int arrayStride, int matrixStride) {
            super(name, id, baseTypeId, typeId, baseType);
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

    public abstract static class DescriptorResource extends Resource {

        public int set, binding;

    }

    public static class BufferDescriptorResource extends DescriptorResource {

        public Struct struct;
        public int baseTypeRaw;
        public Entry.BaseType baseType;

        public int nArrayDim;
        public int[] arrayDim;
        public int arrayStride;

    }

    public static class VertexAttributeResource extends Resource {
        public int location;
        public Entry entry;
    }

    public enum ResourceType {

        UBO(Spvc.SPVC_RESOURCE_TYPE_UNIFORM_BUFFER),
        VAO(Spvc.SPVC_RESOURCE_TYPE_STAGE_INPUT);

        private final int spvc;

        ResourceType(int spvc) {
            this.spvc = spvc;
        }

        public int getSpvc() { return this.spvc; }

    }

}
