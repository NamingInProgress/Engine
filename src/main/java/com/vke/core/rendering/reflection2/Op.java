package com.vke.core.rendering.reflection2;

public class Op {
    // Miscellaneous
    public static final int NOP = 0;
    public static final int UNDEF = 1;
    public static final int SOURCE_CONTINUED = 2;
    public static final int SOURCE = 3;
    public static final int SOURCE_EXTENSION = 4;
    public static final int NAME = 5;
    public static final int MEMBER_NAME = 6;
    public static final int STRING = 7;
    public static final int LINE = 8;

    // Extension & Configuration
    public static final int EXT_INST_IMPORT = 10;
    public static final int EXT_INST = 11;
    public static final int MEMORY_MODEL = 14;
    public static final int ENTRY_POINT = 15;
    public static final int EXECUTION_MODE = 16;
    public static final int CAPABILITY = 17;

    // Structure & Functions
    public static final int TYPE_VOID = 19;
    public static final int TYPE_BOOL = 20;
    public static final int TYPE_INT = 21;
    public static final int TYPE_FLOAT = 22;
    public static final int TYPE_VECTOR = 23;
    public static final int TYPE_MATRIX = 24;
    public static final int TYPE_IMAGE = 25;
    public static final int TYPE_SAMPLER = 26;
    public static final int TYPE_SAMPLED_IMAGE = 27;
    public static final int TYPE_ARRAY = 28;
    public static final int TYPE_RUNTIME_ARRAY = 29;
    public static final int TYPE_STRUCT = 30;
    public static final int TYPE_OPAQUE = 31;
    public static final int TYPE_POINTER = 32;
    public static final int TYPE_FUNCTION = 33;
    public static final int TYPE_EVENT = 34;
    public static final int TYPE_DEVICE_EVENT = 35;
    public static final int TYPE_RESERVE_ID = 36;
    public static final int TYPE_QUEUE = 37;
    public static final int TYPE_PIPE = 38;
    public static final int TYPE_FORWARD_POINTER = 39;

    // Constants
    public static final int CONSTANT_TRUE = 41;
    public static final int CONSTANT_FALSE = 42;
    public static final int CONSTANT = 43;
    public static final int CONSTANT_COMPOSITE = 44;
    public static final int CONSTANT_SAMPLER = 45;
    public static final int CONSTANT_NULL = 46;
    public static final int SPEC_CONSTANT_TRUE = 48;
    public static final int SPEC_CONSTANT_FALSE = 49;
    public static final int SPEC_CONSTANT = 50;
    public static final int SPEC_CONSTANT_COMPOSITE = 51;
    public static final int SPEC_CONSTANT_OP = 52;

    // Memory & Variables
    public static final int VARIABLE = 59;
    public static final int LOAD = 61;
    public static final int STORE = 62;
    public static final int COPY_MEMORY = 63;
    public static final int COPY_MEMORY_SIZED = 64;
    public static final int ACCESS_CHAIN = 65;
    public static final int IN_BOUNDS_ACCESS_CHAIN = 66;
    public static final int PTR_ACCESS_CHAIN = 67;
    public static final int ARRAY_LENGTH = 68;
    public static final int GENERIC_PTR_MEM_SEMANTICS = 69;
    public static final int IN_BOUNDS_PTR_ACCESS_CHAIN = 70;

    // Annotations & Decorations
    public static final int DECORATE = 71;
    public static final int MEMBER_DECORATE = 72;
    public static final int DECORATION_GROUP = 73;
    public static final int GROUP_DECORATE = 74;
    public static final int GROUP_MEMBER_DECORATE = 75;

    // Functions
    public static final int FUNCTION = 54;
    public static final int FUNCTION_PARAMETER = 55;
    public static final int FUNCTION_END = 56;
    public static final int FUNCTION_CALL = 57;

    // Composites
    public static final int COMPOSITE_CONSTRUCT = 79;
    public static final int COMPOSITE_EXTRACT = 80;
    public static final int COMPOSITE_INSERT = 81;
    public static final int COPY_OBJECT = 82;
    public static final int TRANSPOSE = 83;

    // Sampling & Images
    public static final int SAMPLE_D_IMG = 86; // OpSampledImage
    public static final int IMAGE_SAMPLE_IMPLICIT_LOD = 87;
    public static final int IMAGE_SAMPLE_EXPLICIT_LOD = 88;
    public static final int IMAGE_SAMPLE_DREF_IMPLICIT_LOD = 89;
    public static final int IMAGE_SAMPLE_DREF_EXPLICIT_LOD = 90;
    public static final int IMAGE_SAMPLE_PROJ_IMPLICIT_LOD = 91;
    public static final int IMAGE_SAMPLE_PROJ_EXPLICIT_LOD = 92;
    public static final int IMAGE_SAMPLE_PROJ_DREF_IMPLICIT_LOD = 93;
    public static final int IMAGE_SAMPLE_PROJ_DREF_EXPLICIT_LOD = 94;
    public static final int IMAGE_FETCH = 95;
    public static final int IMAGE_GATHER = 96;
    public static final int IMAGE_DREF_GATHER = 97;
    public static final int IMAGE_READ = 98;
    public static final int IMAGE_WRITE = 99;
    public static final int IMAGE = 100;
    public static final int IMAGE_QUERY_FORMAT = 101;
    public static final int IMAGE_QUERY_ORDER = 102;
    public static final int IMAGE_QUERY_SIZE_LOD = 103;
    public static final int IMAGE_QUERY_SIZE = 104;
    public static final int IMAGE_QUERY_LOD = 105;
    public static final int IMAGE_QUERY_LEVELS = 106;
    public static final int IMAGE_QUERY_SAMPLES = 107;

    // Conversions
    public static final int CONVERT_F_TO_U = 109;
    public static final int CONVERT_F_TO_S = 110;
    public static final int CONVERT_ST_TO_F = 111;
    public static final int CONVERT_U_TO_F = 112;
    public static final int U_CONVERT = 113;
    public static final int S_CONVERT = 114;
    public static final int F_CONVERT = 115;
    public static final int QUANTIZE_TO_F16 = 116;
    public static final int CONVERT_PTR_TO_U = 117;
    public static final int SATURATED_CONVERT_U_TO_S = 118;
    public static final int SATURATED_CONVERT_S_TO_U = 119;
    public static final int CONVERT_U_TO_PTR = 120;
    public static final int PTR_CAST_TO_GENERIC = 121;
    public static final int GENERIC_CAST_TO_PTR = 122;
    public static final int GENERIC_CAST_TO_PTR_EXPLICIT = 123;
    public static final int BITCAST = 124;

    // Arithmetic
    public static final int SN_NEGATE = 126; // OpSNegate
    public static final int FN_NEGATE = 127; // OpFNegate
    public static final int I_ADD = 128;
    public static final int F_ADD = 129;
    public static final int I_SUB = 130;
    public static final int F_SUB = 131;
    public static final int I_MUL = 132;
    public static final int F_MUL = 133;
    public static final int U_DIV = 134;
    public static final int S_DIV = 135;
    public static final int F_DIV = 136;
    public static final int U_MOD = 137;
    public static final int S_REM = 138;
    public static final int S_MOD = 139;
    public static final int F_REM = 140;
    public static final int F_MOD = 141;
    public static final int VECTOR_TIMES_SCALAR = 142;
    public static final int MATRIX_TIMES_SCALAR = 143;
    public static final int VECTOR_TIMES_MATRIX = 144;
    public static final int MATRIX_TIMES_VECTOR = 145;
    public static final int MATRIX_TIMES_MATRIX = 146;
    public static final int OUTER_PRODUCT = 147;
    public static final int DOT = 148;
    public static final int I_ADD_CARRY = 149;
    public static final int I_SUB_BORROW = 150;
    public static final int UMUL_EXTENDED = 151;
    public static final int SMUL_EXTENDED = 152;

    // Bitwise
    public static final int SHIFT_RIGHT_LOGICAL = 194;
    public static final int SHIFT_RIGHT_ARITHMETIC = 195;
    public static final int SHIFT_LEFT_LOGICAL = 196;
    public static final int BITWISE_OR = 197;
    public static final int BITWISE_XOR = 198;
    public static final int BITWISE_AND = 199;
    public static final int NOT = 200;
    public static final int BIT_FIELD_INSERT = 201;
    public static final int BIT_FIELD_SEXTRACT = 202;
    public static final int BIT_FIELD_UEXTRACT = 203;
    public static final int BIT_REVERSE = 204;
    public static final int BIT_COUNT = 205;

    // Relational & Logical
    public static final int ANY = 154;
    public static final int ALL = 155;
    public static final int IS_NAN = 156;
    public static final int IS_INF = 157;
    public static final int IS_FINITE = 158;
    public static final int IS_NORMAL = 159;
    public static final int SIGN_BIT_SET = 160;
    public static final int LESS_OR_GREATER = 161;
    public static final int ORDERED = 162;
    public static final int UNORDERED = 163;
    public static final int LOGICAL_EQUAL = 164;
    public static final int LOGICAL_NOT_EQUAL = 165;
    public static final int LOGICAL_OR = 166;
    public static final int LOGICAL_AND = 167;
    public static final int LOGICAL_NOT = 168;
    public static final int I_EQUAL = 169;
    public static final int I_NOT_EQUAL = 170;
    public static final int U_GREATER_THAN = 171;
    public static final int S_GREATER_THAN = 172;
    public static final int U_GREATER_THAN_EQUAL = 173;
    public static final int S_GREATER_THAN_EQUAL = 174;
    public static final int U_LESS_THAN = 175;
    public static final int S_LESS_THAN = 176;
    public static final int U_LESS_THAN_EQUAL = 177;
    public static final int S_LESS_THAN_EQUAL = 178;
    public static final int F_ORD_EQUAL = 179;
    public static final int F_UNORD_EQUAL = 180;
    public static final int F_ORD_NOT_EQUAL = 181;
    public static final int F_UNORD_NOT_EQUAL = 182;
    public static final int F_ORD_LESS_THAN = 183;
    public static final int F_UNORD_LESS_THAN = 184;
    public static final int F_ORD_GREATER_THAN = 185;
    public static final int F_UNORD_GREATER_THAN = 186;
    public static final int F_ORD_LESS_THAN_EQUAL = 187;
    public static final int F_UNORD_LESS_THAN_EQUAL = 188;
    public static final int F_ORD_GREATER_THAN_EQUAL = 189;
    public static final int F_UNORD_GREATER_THAN_EQUAL = 190;

    // Control Flow
    public static final int PHI = 245;
    public static final int LOOP_MERGE = 246;
    public static final int SELECTION_MERGE = 247;
    public static final int LABEL = 248;
    public static final int BRANCH = 249;
    public static final int BRANCH_CONDITIONAL = 250;
    public static final int SWITCH = 251;
    public static final int KILL = 252;
    public static final int RETURN = 253;
    public static final int RETURN_VALUE = 254;
    public static final int UNREACHABLE = 255;

    // Atomic
    public static final int ATOMIC_LOAD = 227;
    public static final int ATOMIC_STORE = 228;
    public static final int ATOMIC_EXCHANGE = 229;
    public static final int ATOMIC_COMPARE_EXCHANGE = 230;
    public static final int ATOMIC_COMPARE_EXCHANGE_WEAK = 231;
    public static final int ATOMIC_I_ADD = 232;
    public static final int ATOMIC_I_SUB = 233;
    public static final int ATOMIC_S_MIN = 234;
    public static final int ATOMIC_U_MIN = 235;
    public static final int ATOMIC_S_MAX = 236;
    public static final int ATOMIC_U_MAX = 237;
    public static final int ATOMIC_AND = 238;
    public static final int ATOMIC_OR = 239;
    public static final int ATOMIC_XOR = 240;

    // Barrier & Derivatives
    public static final int MEMORY_BARRIER = 242;
    public static final int CONTROL_BARRIER = 244;
    public static final int D_PDX = 207; // OpDPdx
    public static final int D_PDY = 208; // OpDPdy
    public static final int FWIDTH = 209;
    public static final int D_PDX_FINE = 210;
    public static final int D_PDY_FINE = 211;
    public static final int FWIDTH_FINE = 212;
    public static final int D_PDX_COARSE = 213;
    public static final int D_PDY_COARSE = 214;
    public static final int FWIDTH_COARSE = 215;

    // Group & Geometric
    public static final int VECTOR_SHUFFLE = 77;
    public static final int COMPOSITE_INSERT_DYNAMIC = 78;
    public static final int GROUP_ASYNC_COPY = 259;
    public static final int GROUP_WAIT_EVENTS = 260;
    public static final int GROUP_ALL = 261;
    public static final int GROUP_ANY = 262;
    public static final int GROUP_BROADCAST = 263;
    public static final int GROUP_I_ADD = 264;
    public static final int GROUP_F_ADD = 265;
    public static final int GROUP_F_MIN = 266;
    public static final int GROUP_UMIN = 267;
    public static final int GROUP_SMIN = 268;
    public static final int GROUP_F_MAX = 269;
    public static final int GROUP_UMAX = 270;
    public static final int GROUP_SMAX = 271;

    // Non-uniform / Later specification additions
    public static final int SELECT = 191;
    public static final int EXTENSION = 12;
    public static final int MODULE_PROCESSED = 330;
    public static final int EXECUTION_MODE_ID = 331;
    public static final int DECORATE_ID = 332;
    public static final int GROUP_NON_UNIFORM_ELECT = 333;
}