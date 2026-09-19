package com.vke.impl.ecs.mesh;

import com.vke.api.rendering.abstraction.renderer.data.StaticMesh;
import com.vke.api.rendering.pbr.Material;
import com.vke.core.ecs.component.Component;
import com.vke.core.rendering.DefaultRenderAssets;
import pl.epsi.EcsComponent;

@EcsComponent
public class StaticMeshC implements Component {

    public StaticMesh[] mesh;
    public Material[] material;
    public int[] renderQueueKey;

    @Override
    public void initialize(int i) {
        mesh[i] = DefaultRenderAssets.defaultMesh();
        material[i] = DefaultRenderAssets.defaultMaterial().copy();
        renderQueueKey[i] = 0;
    }
}
