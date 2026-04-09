package com.vke.core.rendering.bytesenik;

import com.vke.api.rendering.vulkan.buffer.VertexByteSink;

import java.nio.ByteBuffer;
import java.util.Objects;

public class ByteBufferSink implements VertexByteSink {

    private final ByteBuffer buffer;

    public ByteBufferSink(ByteBuffer buffer) {
        Objects.requireNonNull(buffer);
        this.buffer = buffer;
    }

    @Override
    public void int1(int x) {
        buffer.putInt(x);
    }

    @Override
    public void float1(float x) {
        buffer.putFloat(x);
    }

    @Override
    public void uint1(int x) {
        buffer.putInt(x);
    }

    @Override
    public void double1(double x) {
        buffer.putDouble(x);
    }
}
