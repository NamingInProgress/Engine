package com.vke.api.rendering.abstraction.rendergraph;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageUsage;
import com.vke.api.rendering.abstraction.renderer.enums.texture.TextureType;
import com.vke.api.rendering.abstraction.rendergraph.def.RenderPassDefinition;
import com.vke.utils.io.Disposable;

import java.util.ArrayList;
import java.util.List;

public class TexturePool implements Disposable {

    private final RenderSystem system;

    private final List<PooledTexture> freeList = new ArrayList<>();

    public TexturePool(RenderSystem system) {
        this.system = system;
    }

    public Texture acquire(int width, int height, RenderPassDefinition.TextureType type, Format format) {
        if (type == RenderPassDefinition.TextureType.RENDER_TARGET) return system.swapchain().renderTarget();

        for (PooledTexture pooledTexture : freeList) {
            Texture tex = pooledTexture.texture;

            if (tex.width() == width && tex.height() == height && tex.format() == format) {
                freeList.remove(tex);
                return tex;
            }
        }

        return system.device().createTexture(Texture.TextureDesc.builder()
                .width(width)
                .height(height)
                .format(format)
                .usage(getUsage(type))
                .type(TextureType.TEX_2D).build());
    }

    public void release(Texture tex) {
        this.freeList.add(new PooledTexture(tex));
    }

    public void trim(long maxIdleTimeMs) {
        long currentTime = System.currentTimeMillis();

        freeList.removeIf(pooled -> {
            if (currentTime - pooled.lastUsedTime > maxIdleTimeMs) {
                pooled.texture.free();
                return true;
            }
            return false;
        });
    }

    public ImageUsage getUsage(RenderPassDefinition.TextureType type) {
        return switch (type) {
            case RENDER_TARGET -> null;
            case COLOR -> new ImageUsage(ImageUsage.Bits.COLOR_ATTACHMENT_BIT, ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.TRANSFER_SRC_BIT);
            case DEPTH, STENCIL -> new ImageUsage(ImageUsage.Bits.DEPTH_STENCIL_ATTACHMENT_BIT, ImageUsage.Bits.SAMPLED_BIT);
            case STORAGE -> new ImageUsage(ImageUsage.Bits.STORAGE_BIT, ImageUsage.Bits.SAMPLED_BIT);
        };
    }

    @Override
    public void free() {
        // TODO: fix swapchain img getting freed
        //this.freeList.forEach(pt -> pt.texture.free());
    }

    public static class PooledTexture {
        public final Texture texture;
        public long lastUsedTime;

        public PooledTexture(Texture texture) {
            this.texture = texture;
            this.resetUsedTime();
        }

        public void resetUsedTime() {
            this.lastUsedTime = System.currentTimeMillis();
        }
    }

}
