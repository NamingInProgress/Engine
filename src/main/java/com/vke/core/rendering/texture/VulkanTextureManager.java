package com.vke.core.rendering.texture;

import com.vke.api.event.EventListener;
import com.vke.api.event.SubscribeEvent;
import com.vke.api.rendering.abstraction.data.TextureManager;
import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.CISArrayHandle;
import com.vke.core.Context;
import com.vke.core.event.events.assets.AssetLoadEvent;
import com.vke.core.vulkan.descriptor.EngineDescriptorSetsManager;
import com.vke.core.rendering.Samplers;
import com.vke.core.vulkan.service.VulkanRenderSystem;
import com.vke.core.vulkan.service.VulkanRenderer;

import java.util.HashMap;

public class VulkanTextureManager implements TextureManager, EventListener {

    public static int BINDLESS_TEXTURES_COUNT;

    private final HashMap<Texture, Integer> textures = new HashMap<>();
    private final Texture[] bindlessTextures; // TODO: Make this include samplers
    private CISArrayHandle BINDLESS_HANDLE;

    private final EngineDescriptorSetsManager mgr;
    private final Context ctx;
    private final VulkanRenderer renderer;

    private Sampler sampler;

    public VulkanTextureManager(VulkanRenderSystem ctx, EngineDescriptorSetsManager mgr, int bindlessTexturesCount) {
        BINDLESS_TEXTURES_COUNT = bindlessTexturesCount;
        bindlessTextures = new Texture[BINDLESS_TEXTURES_COUNT];
        this.mgr = mgr;
        this.ctx = ctx;
        this.renderer = ctx.service(ctx.getEngine().rendererType().serviceName).assumeImplementation();
        this.sampler = Samplers.LINEAR;
    }

    @SubscribeEvent
    public void onAssetLoad(AssetLoadEvent e) {
        var obj = e.desc.handle().get();
        if (obj instanceof Texture tex) {
            registerTexture(tex);
        }
    }

    @Override
    public int registerTexture(Texture tex) {
        if (textures.containsKey(tex)) return textures.get(tex);

        if (BINDLESS_HANDLE == null) BINDLESS_HANDLE = mgr.ENGINE_PIPELINE_LAYOUT.getGroup().resolve("textures");
        int firstFree = -1;
        for (int i = 0; i < bindlessTextures.length; i++) {
            if (bindlessTextures[i] == null) {
                firstFree = i;
                break;
            }
        }
        if (firstFree == -1) throw new IllegalStateException("Out of texture slots!");

        bindlessTextures[firstFree] = tex;
        textures.put(tex, firstFree);
        BINDLESS_HANDLE.set(tex, sampler, firstFree);
        renderer.scheduleDescriptorUpdate(mgr.ENGINE_PIPELINE_LAYOUT, BINDLESS_HANDLE);
        return firstFree;
    }

    @Override
    public int texture(Texture tex) {
        if (tex == null) return -1;
        int id = textures.getOrDefault(tex, -1);
        if (id == -1) ctx.getLogger().warn("Failed to acquire texture ID for texture " + tex + ", was it registered?");
        return id;
    }

    @Override
    public void removeTexture(Texture tex) {
        // TODO: implement
    }

    @Override
    public void withSampler(Sampler sampler) {
        this.sampler = sampler;
    }

}
