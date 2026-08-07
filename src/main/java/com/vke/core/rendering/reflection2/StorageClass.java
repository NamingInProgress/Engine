package com.vke.core.rendering.reflection2;

public final class StorageClass {

    private StorageClass() {}

    public static final int UNIFORM_CONSTANT = 0;
    public static final int INPUT = 1;
    public static final int UNIFORM = 2;
    public static final int OUTPUT = 3;
    public static final int WORKGROUP = 4;
    public static final int CROSS_WORKGROUP = 5;
    public static final int PRIVATE = 6;
    public static final int FUNCTION = 7;
    public static final int GENERIC = 8;
    public static final int PUSH_CONSTANT = 9;
    public static final int ATOMIC_COUNTER = 10;
    public static final int IMAGE = 11;
    public static final int STORAGE_BUFFER = 12;
    public static final int TILE_IMAGE_EXT = 4172;
    public static final int NODE_PAYLOAD_AMDX = 5068;
    public static final int CALLABLE_DATA_KHR = 5328;
    public static final int INCOMING_CALLABLE_DATA_KHR = 5329;
    public static final int RAY_PAYLOAD_KHR = 5338;
    public static final int HIT_ATTRIBUTE_KHR = 5339;
    public static final int INCOMING_RAY_PAYLOAD_KHR = 5342;
    public static final int SHADER_RECORD_BUFFER_KHR = 5343;
    public static final int PHYSICAL_STORAGE_BUFFER = 5349;
    public static final int TASK_PAYLOAD_WORKGROUP_EXT = 5402;
    public static final int CODE_SECTION_INTEL = 5605;
    public static final int DEVICE_ONLY_INTEL = 5936;
    public static final int HOST_ONLY_INTEL = 5937;
}
