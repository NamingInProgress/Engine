package com.vke.api.abstraction.descriptors;

public class DeviceCapabilities {

    /** LIMITS **/
    int maxTexture2DSize;
    int maxTexture3DSize;
    int maxCubeMapSize;

    int maxUBOSize;
    int maxSSBOSize;

    int maxColorAttachments;
    int maxDescriptorSets;
    int maxBindingsPerSet;

    int maxVertexAttributes;
    int maxVertexBuffers;

    long maxBufferSize;

    /** GPU INFO **/
    GpuType gpuType;

}
