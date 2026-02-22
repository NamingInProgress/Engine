package com.vke.api.abstraction.descriptors;

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

    /** GPU INFO **/
    public GpuType gpuType;

}
