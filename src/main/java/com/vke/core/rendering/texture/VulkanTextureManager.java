package com.vke.core.rendering.texture;

import com.vke.api.rendering.abstraction.data.ITextureManager;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.vulkan.descriptors2.handles.other.array.CISArrayHandle;
import com.vke.core.vulkan.descriptor.EngineDescriptorSetsManager;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.sampler.Samplers;

public class VulkanTextureManager implements ITextureManager {

    public static int BINDLESS_TEXTURES_COUNT;

    private final Texture[] bindlessTextures;
    private CISArrayHandle BINDLESS_HANDLE;

    private final EngineDescriptorSetsManager mgr;

    public VulkanTextureManager(EngineDescriptorSetsManager mgr, VulkanRenderDevice device) {
        BINDLESS_TEXTURES_COUNT = Math.min(device.capabilities().maxBindlessSampledImages, 8192);
        bindlessTextures = new Texture[BINDLESS_TEXTURES_COUNT];
        this.mgr = mgr;
    }

    @Override
    public int texture(Texture tex) {
        if (BINDLESS_HANDLE == null) BINDLESS_HANDLE = mgr.ENGINE_PIPELINE_LAYOUT.getGroup().resolve("textures");
        int firstFree = -1;
        for (int i = 0; i < bindlessTextures.length; i++) {
            if (bindlessTextures[i] == tex) {
                BINDLESS_HANDLE.set(tex, Samplers.LINEAR, i);
                return i;
            }
            if (bindlessTextures[i] == null && firstFree == -1) {
                firstFree = i;
            }
        }
        if (firstFree == -1) throw new IllegalStateException("Out of texture slots!");

        bindlessTextures[firstFree] = tex;
        BINDLESS_HANDLE.set(tex, Samplers.LINEAR, firstFree);
        return firstFree;
    }

    @Override
    public void removeTexture(Texture tex) {
        // TODO: implement
    }

}
