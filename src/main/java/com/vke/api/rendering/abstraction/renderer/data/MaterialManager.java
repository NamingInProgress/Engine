package com.vke.api.rendering.abstraction.renderer.data;

import com.vke.api.rendering.pbr.Material;

public interface MaterialManager {
    void registerMaterial(Material mat);
    int material(Material mat);
    void removeMaterial(Material mat);
}
