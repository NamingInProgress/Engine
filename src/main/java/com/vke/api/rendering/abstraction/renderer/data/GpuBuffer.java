package com.vke.api.rendering.abstraction.renderer.data;

import com.vke.api.rendering.abstraction.renderer.enums.buffer.BufferUsage;
import com.vke.api.rendering.abstraction.renderer.enums.buffer.MemoryUsage;
import com.vke.utils.io.Disposable;

public interface GpuBuffer extends Disposable {

    record Description(long size, BufferUsage usage, MemoryUsage memUsage, int... flags) {}

    long size();
    BufferUsage usage();
    MemoryUsage memoryUsage();

    //void upload(long offset, ByteBuffer data);

    //boolean isMapped();
    //ByteBuffer map();
    //void unmap();

}
