package com.vke.core.rendering.graph2;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageUsage;
import com.vke.api.rendering.abstraction.renderer.enums.texture.TextureType;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.rendering.vulkan.device.VulkanRenderDevice;
import com.vke.core.rendering.vulkan.texture.VulkanTexture;
import com.vke.core.rendering.vulkan.utils.VKUtils;
import com.vke.utils.io.Disposable;
import org.lwjgl.vulkan.VK14;

import java.util.LinkedList;
import java.util.List;

public class TexturePool implements Disposable {

    private final RenderSystem system;

    private final List<PooledTexture> freeList = new LinkedList<>();
    private final List<Texture> allAllocated = new LinkedList<>();

    public TexturePool(RenderSystem system) {
        this.system = system;
    }

    public Texture[] getAllRenderTargets() {
        return system.swapchain().renderTargets();
    }

    public Texture acquire(int width, int height, RenderPass.TextureType type, Format format) {
        if (type == RenderPass.TextureType.SCREEN) return system.swapchain().renderTarget();

        for (PooledTexture pooledTexture : freeList) {
            Texture tex = pooledTexture.texture;
            if (tex == null) continue;

            if (tex.width() == width && tex.height() == height && tex.format().equals(format)) {
                freeList.remove(pooledTexture);
                return tex;
            }
        }

        var t = system.device().createTexture(Texture.TextureDesc.builder()
                .width(width)
                .height(height)
                .format(format)
                .usage(getUsage(type))
                .type(TextureType.TEX_2D).build());

        if (system.getEngine().isDebugMode()) {
//            VKUtils.setDebugName(((VulkanRenderDevice) system.device()).getLogicalDevice(), "pool texture", ((VulkanTexture) t).getHandle(), VK14.VK_OBJECT_TYPE_IMAGE);
        }

        allAllocated.add(t);
        return t;
    }

    public void release(Texture tex) {
        this.freeList.add(new PooledTexture(tex));
    }

    public void trim(long maxIdleTimeMs) {
        long currentTime = System.currentTimeMillis();

        freeList.removeIf(pooled -> {
            if (currentTime - pooled.lastUsedTime > maxIdleTimeMs) {
                pooled.texture.free();
                allAllocated.remove(pooled.texture);
                return true;
            }
            return false;
        });
    }

    public ImageUsage getUsage(RenderPass.TextureType type) {
        return switch (type) {
            case SCREEN -> null;
            case COLOR -> new ImageUsage(ImageUsage.Bits.COLOR_ATTACHMENT_BIT, ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.TRANSFER_SRC_BIT);
            case DEPTH, STENCIL, DEPTH_STENCIL -> new ImageUsage(ImageUsage.Bits.DEPTH_STENCIL_ATTACHMENT_BIT, ImageUsage.Bits.SAMPLED_BIT);
            case STORAGE -> new ImageUsage(ImageUsage.Bits.STORAGE_BIT, ImageUsage.Bits.SAMPLED_BIT);
        };
    }

    @Override
    public void free() {
        this.allAllocated.forEach(Disposable::free);
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
