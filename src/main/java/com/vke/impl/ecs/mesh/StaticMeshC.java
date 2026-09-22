package com.vke.impl.ecs.mesh;

import com.vke.api.rendering.abstraction.renderer.data.StaticMesh;
import com.vke.api.rendering.pbr.Material;
import com.vke.core.ecs.component.Component;
import com.vke.core.rendering.DefaultRenderAssets;
import pl.epsi.EcsComponent;

@EcsComponent
public class StaticMeshC implements Component {

    public StaticMesh[] mesh;
    public int[] material;
    public int[] renderQueueKey;

    @Override
    public void initialize(int i) {
        mesh[i] = DefaultRenderAssets.defaultMesh();
        material[i] = DefaultRenderAssets.copyDefaultMaterialIndex();
        renderQueueKey[i] = 0;
    }

    public void setMesh(int i, StaticMesh mesh) {
        this.mesh[i] = mesh;
    }

    public void setMaterial(int i, Material material) {
        this.material[i] = DefaultRenderAssets.getRenderSystem().materialManager().material(material);
    }

    public void changeRenderQueue(int i, int renderQueue) {
        this.renderQueueKey[i] = renderQueue;
    }
}
