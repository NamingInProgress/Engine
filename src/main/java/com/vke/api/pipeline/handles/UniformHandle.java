package com.vke.api.pipeline.handles;

import com.vke.api.abstraction.data.Sampler;
import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.descriptors.buffer.PackingType;
import com.vke.api.pipeline.DescriptorData;
import com.vke.core.vulkan.buffers.premade.BufferSlice;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class UniformHandle extends DataHandle {

    public int set;
    public int binding;
    public int size;
    public long offset;
    public PackingType packing;
    public DescriptorData.Binding.Type bindingType;

    public long buffer;
    public long gpuBuffer;
    public @Nullable Texture[] textures;
    public @Nullable Sampler[] samplers;

    public Consumer<UniformHandle> flushCallback = (a) -> {};

    public void write(Consumer<BufferSlice> consumer) {
        consumer.accept(new BufferSlice(buffer, offset, size, packing));
    }

    public void setSampler(Sampler s, Texture t) {
        this.setSampler(s, t, 0, true);
    }

    public void setSampler(Sampler s, Texture t, int index) {
        this.setSampler(s, t, index, true);
    }

    public void setSampler(Sampler s, Texture t, boolean flush) {
        this.setSampler(s, t, 0, flush);
    }

    public void setSampler(Sampler s, Texture t, int index, boolean flush) {
        textures[index] = t;
        samplers[index] = s;
        if (flush) flush();
    }

    public void setImage(Texture t) {
        this.setImage(t, 0, true);
    }

    public void setImage(Texture t, int index) {
        this.setImage(t, index, true);
    }

    public void setImage(Texture t, boolean flush) {
        this.setImage(t, 0, flush);
    }

    public void setImage(Texture t, int index, boolean flush) {
        textures[index] = t;
        if (flush) flush();
    }

    public void flush() {
        this.flushCallback.accept(this);
    }

}
