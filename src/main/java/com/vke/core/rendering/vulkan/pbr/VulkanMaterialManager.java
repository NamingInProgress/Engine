package com.vke.core.rendering.vulkan.pbr;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.carrotsearch.hppc.cursors.ObjectIntCursor;
import com.vke.api.event.EventListener;
import com.vke.api.event.SubscribeEvent;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.ByteEncoder;
import com.vke.api.rendering.abstraction.renderer.data.MaterialManager;
import com.vke.api.rendering.abstraction.renderer.data.RenderingEncoder;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.api.rendering.pbr.Material;
import com.vke.core.Context;
import com.vke.core.event.events.assets.AssetLoadEvent;
import com.vke.core.rendering.vulkan.buffers.MappedBuffer;
import com.vke.core.rendering.vulkan.descriptor.ds2.DescriptorSetInstance;

public class VulkanMaterialManager implements MaterialManager, EventListener {

    private static final int MATERIAL_MAX_COUNT = 1024;

    private final ObjectIntHashMap<Material> materials = new ObjectIntHashMap<>();
    private final Material[] mats = new Material[MATERIAL_MAX_COUNT];

    private final RenderSystem ctx;

    private boolean dirty;

    public VulkanMaterialManager(RenderSystem ctx) {
        this.ctx = ctx;
    }

    @SubscribeEvent
    public void onAssetLoad(AssetLoadEvent e) {
        var obj = e.desc.handle().get();
        if (obj instanceof Material mat) {
            registerMaterial(mat);
        }
    }

    @Override
    public void registerMaterial(Material mat) {
        if (materials.containsKey(mat)) return;

        int firstFree = -1;
        for (int i = 0; i < mats.length; i++) {
            if (mats[i] == null) {
                firstFree = i;
                break;
            }
        }
        if (firstFree == -1) throw new IllegalStateException("Out of material slots!");

        mats[firstFree] = mat;
        materials.put(mat, firstFree);
        this.dirty = true;
    }

    @Override
    public int material(Material mat) {
        if (mat == null) return -1;
        int id = materials.getOrDefault(mat, -1);
        if (id == -1) ctx.getLogger().warn("Failed to acquire material ID for material " + mat + ", was it registered?");
        return id;
    }

    @Override
    public void removeMaterial(Material mat) {
        // TODO: implement, i am not doing ts bro :pray:
    }

    @Override
    public void upload(FieldArrayResource ssbo) {
        if (this.dirty) {
            for (ObjectIntCursor<Material> cursor : materials) {
                ssbo.write(cursor.value, cursor.key::putSelf);
            }

            this.dirty = false;
        }
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

}
