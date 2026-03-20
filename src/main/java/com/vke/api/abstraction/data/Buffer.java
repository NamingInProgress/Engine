package com.vke.api.abstraction.data;

import com.vke.api.abstraction.descriptors.buffer.BufferUsage;
import com.vke.api.abstraction.descriptors.buffer.MemoryUsage;
import com.vke.utils.io.Disposable;

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
