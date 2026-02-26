package com.vke.api.rendering.abstraction.data;

import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.abstraction.enums.buffer.MemoryUsage;
import com.vke.utils.Disposable;

public interface Buffer extends Disposable {

    record Description(long size, BufferUsage usage, MemoryUsage memUsage) {}

    long size();
    BufferUsage usage();
    MemoryUsage memoryUsage();

    //void upload(long offset, ByteBuffer data);

    //boolean isMapped();
    //ByteBuffer map();
    //void unmap();

}
