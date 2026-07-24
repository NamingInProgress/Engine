package com.vke.core.rendering.vulkan.pbr;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.event.EventListener;
import com.vke.api.event.SubscribeEvent;
import com.vke.api.rendering.abstraction.renderer.data.MaterialManager;
import com.vke.api.rendering.pbr.Material;
import com.vke.core.Context;
import com.vke.core.event.events.assets.AssetLoadEvent;

public class VulkanMaterialManager implements MaterialManager, EventListener {

    private static final int MATERIAL_MAX_COUNT = 1024;

    private final ObjectIntHashMap<Material> materials = new ObjectIntHashMap<>();
    private final Material[] mats = new Material[MATERIAL_MAX_COUNT];

    private final Context ctx;

    public VulkanMaterialManager(Context ctx) {
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
    }

    @Override
    public int material(Material mat) {
        if (mat == null) return -1;
        int id = materials.getOrDefault(mat, -1);
        if (id == -1) ctx.getLogger().warn("Failed to acquire texture ID for texture " + mat + ", was it registered?");
        return id;
    }

    @Override
    public void removeMaterial(Material mat) {
        // TODO: implement, i am not doing ts bro :pray:
    }
}
