package com.vke.api.rendering.abstraction.renderer.data;

import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.api.rendering.pbr.Material;

public interface MaterialManager {
    int registerMaterial(Material mat);
    int material(Material mat);
    void removeMaterial(Material mat);

    void upload(FieldArrayResource ssbo);

    boolean isDirty();
    void setDirty(boolean dirty);
}
