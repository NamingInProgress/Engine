package com.vke.api.draw;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.vulkan.buffer.VertexByteSink;
import org.jetbrains.annotations.Nullable;

public abstract class Vertex {
    protected int texId = -1;

    public abstract int getByteStride();
    public abstract void putSelf(VertexByteSink buf);
    public @Nullable Texture usesTexture() { return null; }

    public int texId() { return texId; }

    public void setTextureId(int texId) {
        if (usesTexture() == null) {
            this.texId = -1;
        } else {
            this.texId = texId;
        }
    }
}