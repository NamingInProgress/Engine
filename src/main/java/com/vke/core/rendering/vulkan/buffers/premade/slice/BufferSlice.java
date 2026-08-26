package com.vke.core.rendering.vulkan.buffers.premade.slice;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.ByteEncoder;
import com.vke.api.rendering.abstraction.renderer.data.RenderingEncoder;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.buffer.PackingType;
import com.vke.api.rendering.pbr.Material;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class BufferSlice implements ByteEncoder {
    protected final long bufferAddress;
    protected final long offset;
    protected final int length;
    protected final long writeAddress;
    protected final PackingType packing;

    private int cursor;

    public BufferSlice(long bufferAddress, long offset, int length, PackingType packingType) {
        this.bufferAddress = bufferAddress;
        this.offset = offset;
        this.length = length;
        this.writeAddress = bufferAddress + offset;
        this.packing = packingType;
    }

    public void skip(int amount) {
        if (cursor + amount > length)
            throw new IndexOutOfBoundsException(
                    "BufferSlice overflow: " + cursor + " / " + length
            );
        cursor += amount;
    }

    public void data(ByteBuffer buf) {
        if (cursor + buf.remaining() > length)
            throw new IndexOutOfBoundsException(
                    "BufferSlice overflow: " + cursor + " / " + length
            );
        MemoryUtil.memCopy(MemoryUtil.memAddress(buf), writeAddress + cursor, buf.remaining());
        cursor += buf.remaining();
    }

    public void int1At(int byteOffset, int val) {
        if (byteOffset + 4 > length)
            throw new IndexOutOfBoundsException(
                    "BufferSlice overflow: " + byteOffset + " / " + length
            );
        MemoryUtil.memPutInt(writeAddress + byteOffset, val);
    }

    public void mat4(Matrix4f m) {
        float1(m.m00());
        float1(m.m01());
        float1(m.m02());
        float1(m.m03());

        // Column 1
        float1(m.m10());
        float1(m.m11());
        float1(m.m12());
        float1(m.m13());

        // Column 2
        float1(m.m20());
        float1(m.m21());
        float1(m.m22());
        float1(m.m23());

        // Column 3
        float1(m.m30());
        float1(m.m31());
        float1(m.m32());
        float1(m.m33());
    }

    public void mat3(Matrix3f m) {
        // column 0
        float1(m.m00());
        float1(m.m01());
        float1(m.m02());
        float1(0.0f);

        // column 1
        float1(m.m10());
        float1(m.m11());
        float1(m.m12());

        float1(0.0f);


        // column 2
        float1(m.m20());
        float1(m.m21());
        float1(m.m22());

        float1(0.0f);
    }

    public void mat2(Matrix2f m) {
        // column 0
        float1(m.m00());
        float1(m.m01());
        // column 1
        float1(m.m10());
        float1(m.m11());
    }
    
    public void sampler2D(RenderSystem sys, @Nullable Texture texture) {
        int1(sys.textureManager().texture(texture));
    }
    
    public void material(RenderSystem sys, @Nullable Material material) {
        int1(sys.materialManager().material(material));
    }

    @Override
    public void float1(float x) {
        if (cursor + 4 > length)
            throw new IndexOutOfBoundsException(
                    "BufferSlice overflow: " + cursor + " / " + length
            );
        MemoryUtil.memPutFloat(writeAddress + cursor, x);
        cursor += 4;
    }

    @Override
    public void int1(int x) {
        if (cursor + 4 > length)
            throw new IndexOutOfBoundsException(
                    "BufferSlice overflow: " + cursor + " / " + length
            );
        MemoryUtil.memPutInt(writeAddress + cursor, x);
        cursor += 4;
    }

    @Override
    public void uint1(int x) {
        int1(x);
    }

    @Override
    public void double1(double x) {
        if (cursor + 8 > length) throw new IndexOutOfBoundsException(
                "BufferSlice overflow: " + cursor + " / " + length
        );
        MemoryUtil.memPutDouble(writeAddress + cursor, x);
        cursor += 8;
    }

}
