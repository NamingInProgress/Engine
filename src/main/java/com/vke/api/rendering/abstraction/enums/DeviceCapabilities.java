package com.vke.api.rendering.abstraction.enums;

public class DeviceCapabilities {

    /** LIMITS **/
    public int maxTexture2DSize;
    public int maxTexture3DSize;
    public int maxCubeMapSize;

    public int maxUBOSize;
    public int maxSSBOSize;

    public int maxColorAttachments;
    public int maxDescriptorSets;

    public int maxVertexAttributes;

    public int maxPushConstantSize;

    public long minUboAlign;
    /** GPU INFO **/
    public GpuType gpuType;

}
