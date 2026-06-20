package com.vke.core.rendering.reflection2;

public class Decoration {
    public static final int RELAXED_PRECISION = 0;
    public static final int SPEC_ID = 1;
    public static final int BLOCK = 2;
    public static final int BUFFER_BLOCK = 3;
    public static final int ROW_MAJOR = 4;
    public static final int COL_MAJOR = 5;
    public static final int ARRAY_STRIDE = 6;
    public static final int MATRIX_STRIDE = 7;
    public static final int GLSL_SHARED = 8;
    public static final int GLSL_PACKED = 9;
    public static final int C_PACKED = 10;
    public static final int BUILT_IN = 11;

    public static final int NO_PERSPECTIVE = 13;
    public static final int FLAT = 14;
    public static final int PATCH = 15;
    public static final int CENTROID = 16;
    public static final int SAMPLE = 17;
    public static final int INVARIANT = 18;
    public static final int RESTRICT = 19;
    public static final int ALIASED = 20;
    public static final int VOLATILE = 21;
    public static final int CONSTANT = 22;
    public static final int COHERENT = 23;
    public static final int NON_WRITABLE = 24;
    public static final int NON_READABLE = 25;
    public static final int UNIFORM = 26;
    public static final int UNIFORM_ID = 27;
    public static final int SATURATED_CONVERSION = 28;
    public static final int STREAM = 29;
    public static final int LOCATION = 30;
    public static final int COMPONENT = 31;
    public static final int INDEX = 32;
    public static final int BINDING = 33;
    public static final int DESCRIPTOR_SET = 34;
    public static final int OFFSET = 35;
    public static final int XFB_BUFFER = 36;
    public static final int XFB_STRIDE = 37;
    public static final int FUNC_PARAM_ATTR = 38;
    public static final int FP_ROUNDING_MODE = 39;
    public static final int FP_FAST_MATH_MODE = 40;
    public static final int LINKAGE_ATTRIBUTES = 41;
    public static final int NO_CONTRACTION = 42;
    public static final int INPUT_ATTACHMENT_INDEX = 43;
    public static final int ALIGNMENT = 44;
    public static final int MAX_BYTE_OFFSET = 45;
    public static final int ALIGNMENT_ID = 46;
    public static final int MAX_BYTE_OFFSET_ID = 47;

    public static final int SATURATED_TO_LARGEST_FLOAT8_NORMAL_CONVERSION_EXT = 4216;

    public static final int NO_SIGNED_WRAP = 4469;
    public static final int NO_UNSIGNED_WRAP = 4470;
    public static final int WEIGHT_TEXTURE_QCOM = 4487;
    public static final int BLOCK_MATCH_TEXTURE_QCOM = 4488;
    public static final int BLOCK_MATCH_SAMPLER_QCOM = 4499;

    public static final int EXPLICIT_INTERP_AMD = 4999;
    public static final int NODE_SHARES_PAYLOAD_LIMITS_WITH_AMDX = 5019;
    public static final int NODE_MAX_PAYLOADS_AMDX = 5020;

    public static final int TRACK_FINISH_WRITING_AMDX = 5078;
    public static final int PAYLOAD_NODE_NAME_AMDX = 5091;
    public static final int PAYLOAD_NODE_BASE_INDEX_AMDX = 5098;
    public static final int PAYLOAD_NODE_SPARSE_ARRAY_AMDX = 5099;
    public static final int PAYLOAD_NODE_ARRAY_SIZE_AMDX = 5100;
    public static final int PAYLOAD_DISPATCH_INDIRECT_AMDX = 5105;

    public static final int ARRAY_STRIDE_ID_EXT = 5124;
    public static final int OFFSET_ID_EXT = 5125;

    public static final int OVERRIDE_COVERAGE_NV = 5248;
    public static final int PASSTHROUGH_NV = 5250;
    public static final int VIEWPORT_RELATIVE_NV = 5252;
    public static final int SECONDARY_VIEWPORT_RELATIVE_NV = 5256;

    public static final int PER_PRIMITIVE_EXT = 5271;
    public static final int PER_PRIMITIVE_NV = 5271;
    public static final int PER_VIEW_NV = 5272;
    public static final int PER_TASK_NV = 5273;
    public static final int PER_VERTEX_KHR = 5285;
    public static final int PER_VERTEX_NV = 5285;

    public static final int NON_UNIFORM = 5300;
    public static final int NON_UNIFORM_EXT = 5300;

    public static final int RESTRICT_POINTER = 5355;
    public static final int RESTRICT_POINTER_EXT = 5355;
    public static final int ALIASED_POINTER = 5356;
    public static final int ALIASED_POINTER_EXT = 5356;
    public static final int MEMBER_OFFSET_NV = 5358;

    public static final int HIT_OBJECT_SHADER_RECORD_BUFFER_NV = 5386;
    public static final int HIT_OBJECT_SHADER_RECORD_BUFFER_EXT = 5389;
    public static final int BANK_NV = 5397;
    public static final int BINDLESS_SAMPLER_NV = 5398;
    public static final int BINDLESS_IMAGE_NV = 5399;
    public static final int BOUND_SAMPLER_NV = 5400;
    public static final int BOUND_IMAGE_NV = 5401;

    public static final int SIMT_CALL_INTEL = 5599;
    public static final int REFERENCED_INDIRECTLY_INTEL = 5602;
    public static final int CLOBBER_INTEL = 5607;
    public static final int SIDE_EFFECTS_INTEL = 5608;

    public static final int VECTOR_COMPUTE_VARIABLE_INTEL = 5624;
    public static final int FUNC_PARAM_IO_KIND_INTEL = 5625;
    public static final int VECTOR_COMPUTE_FUNCTION_INTEL = 5626;
    public static final int STACK_CALL_INTEL = 5627;
    public static final int GLOBAL_VARIABLE_OFFSET_INTEL = 5628;

    public static final int COUNTER_BUFFER = 5634;
    public static final int HLSL_COUNTER_BUFFER_GOOGLE = 5634;
    public static final int USER_SEMANTIC = 5635;
    public static final int HLSL_SEMANTIC_GOOGLE = 5635;
    public static final int USER_TYPE_GOOGLE = 5636;

    public static final int FUNCTION_ROUNDING_MODE_INTEL = 5822;
    public static final int FUNCTION_DENORM_MODE_INTEL = 5823;

    public static final int REGISTER_ALTERA = 5825;
    public static final int REGISTER_INTEL = 5825;
    public static final int MEMORY_ALTERA = 5826;
    public static final int MEMORY_INTEL = 5826;
    public static final int NUMBANKS_ALTERA = 5827;
    public static final int NUMBANKS_INTEL = 5827;
    public static final int BANKWIDTH_ALTERA = 5828;
    public static final int BANKWIDTH_INTEL = 5828;
    public static final int MAX_PRIVATE_COPIES_ALTERA = 5829;
    public static final int MAX_PRIVATE_COPIES_INTEL = 5829;
    public static final int SINGLEPUMP_ALTERA = 5830;
    public static final int SINGLEPUMP_INTEL = 5830;
    public static final int DOUBLEPUMP_ALTERA = 5831;
    public static final int DOUBLEPUMP_INTEL = 5831;
    public static final int MAX_REPLICATES_ALTERA = 5832;
    public static final int MAX_REPLICATES_INTEL = 5832;
    public static final int SIMPLE_DUAL_PORT_ALTERA = 5833;
    public static final int SIMPLE_DUAL_PORT_INTEL = 5833;
    public static final int MERGE_ALTERA = 5834;
    public static final int MERGE_INTEL = 5834;
    public static final int BANK_BITS_ALTERA = 5835;
    public static final int BANK_BITS_INTEL = 5835;
    public static final int FORCE_POW2_DEPTH_ALTERA = 5836;
    public static final int FORCE_POW2_DEPTH_INTEL = 5836;

    public static final int STRIDESIZE_ALTERA = 5883;
    public static final int STRIDESIZE_INTEL = 5883;
    public static final int WORDSIZE_ALTERA = 5884;
    public static final int WORDSIZE_INTEL = 5884;
    public static final int TRUE_DUAL_PORT_ALTERA = 5885;
    public static final int TRUE_DUAL_PORT_INTEL = 5885;

    public static final int BURST_COALESCE_ALTERA = 5899;
    public static final int BURST_COALESCE_INTEL = 5899;
    public static final int CACHE_SIZE_ALTERA = 5900;
    public static final int CACHE_SIZE_INTEL = 5900;
    public static final int DONT_STATICALLY_COALESCE_ALTERA = 5901;
    public static final int DONT_STATICALLY_COALESCE_INTEL = 5901;
    public static final int PREFETCH_ALTERA = 5902;
    public static final int PREFETCH_INTEL = 5902;

    public static final int STALL_ENABLE_ALTERA = 5905;
    public static final int STALL_ENABLE_INTEL = 5905;
    public static final int FUSE_LOOPS_IN_FUNCTION_ALTERA = 5907;
    public static final int FUSE_LOOPS_IN_FUNCTION_INTEL = 5907;
    public static final int MATH_OP_DSP_MODE_ALTERA = 5909;
    public static final int MATH_OP_DSP_MODE_INTEL = 5909;

    public static final int ALIAS_SCOPE_INTEL = 5914;
    public static final int NO_ALIAS_INTEL = 5915;

    public static final int INITIATION_INTERVAL_ALTERA = 5917;
    public static final int INITIATION_INTERVAL_INTEL = 5917;
    public static final int MAX_CONCURRENCY_ALTERA = 5918;
    public static final int MAX_CONCURRENCY_INTEL = 5918;
    public static final int PIPELINE_ENABLE_ALTERA = 5919;
    public static final int PIPELINE_ENABLE_INTEL = 5919;

    public static final int BUFFER_LOCATION_ALTERA = 5921;
    public static final int BUFFER_LOCATION_INTEL = 5921;

    public static final int IO_PIPE_STORAGE_ALTERA = 5944;
    public static final int IO_PIPE_STORAGE_INTEL = 5944;

    public static final int FUNCTION_FLOATING_POINT_MODE_INTEL = 6080;
    public static final int SINGLE_ELEMENT_VECTOR_INTEL = 6085;
    public static final int VECTOR_COMPUTE_CALLABLE_FUNCTION_INTEL = 6087;
    public static final int MEDIA_BLOCK_IO_INTEL = 6140;

    public static final int STALL_FREE_ALTERA = 6151;
    public static final int STALL_FREE_INTEL = 6151;

    public static final int FP_MAX_ERROR_DECORATION_INTEL = 6170;

    public static final int LATENCY_CONTROL_LABEL_ALTERA = 6172;
    public static final int LATENCY_CONTROL_LABEL_INTEL = 6172;
    public static final int LATENCY_CONTROL_CONSTRAINT_ALTERA = 6173;
    public static final int LATENCY_CONTROL_CONSTRAINT_INTEL = 6173;

    public static final int CONDUIT_KERNEL_ARGUMENT_ALTERA = 6175;
    public static final int CONDUIT_KERNEL_ARGUMENT_INTEL = 6175;
    public static final int REGISTER_MAP_KERNEL_ARGUMENT_ALTERA = 6176;
    public static final int REGISTER_MAP_KERNEL_ARGUMENT_INTEL = 6176;
    public static final int MM_HOST_INTERFACE_ADDRESS_WIDTH_ALTERA = 6177;
    public static final int MM_HOST_INTERFACE_ADDRESS_WIDTH_INTEL = 6177;
    public static final int MM_HOST_INTERFACE_DATA_WIDTH_ALTERA = 6178;
    public static final int MM_HOST_INTERFACE_DATA_WIDTH_INTEL = 6178;
    public static final int MM_HOST_INTERFACE_LATENCY_ALTERA = 6179;
    public static final int MM_HOST_INTERFACE_LATENCY_INTEL = 6179;
    public static final int MM_HOST_INTERFACE_READ_WRITE_MODE_ALTERA = 6180;
    public static final int MM_HOST_INTERFACE_READ_WRITE_MODE_INTEL = 6180;
    public static final int MM_HOST_INTERFACE_MAX_BURST_ALTERA = 6181;
    public static final int MM_HOST_INTERFACE_MAX_BURST_INTEL = 6181;
    public static final int MM_HOST_INTERFACE_WAIT_REQUEST_ALTERA = 6182;
    public static final int MM_HOST_INTERFACE_WAIT_REQUEST_INTEL = 6182;
    public static final int STABLE_KERNEL_ARGUMENT_ALTERA = 6183;
    public static final int STABLE_KERNEL_ARGUMENT_INTEL = 6183;

    public static final int HOST_ACCESS_INTEL = 6188;

    public static final int INIT_MODE_ALTERA = 6190;
    public static final int INIT_MODE_INTEL = 6190;
    public static final int IMPLEMENT_IN_REGISTER_MAP_ALTERA = 6191;
    public static final int IMPLEMENT_IN_REGISTER_MAP_INTEL = 6191;

    public static final int CONDITIONAL_INTEL = 6247;

    public static final int CACHE_CONTROL_LOAD_INTEL = 6442;
    public static final int CACHE_CONTROL_STORE_INTEL = 6443;

    public static final int MAX = 0x7fffffff;
}
