package com.vke.core.rendering.rp;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.services2.Services;
import com.vke.impl.ecs.mesh.StaticMeshC;

public class MeshCollector {

    private final int CATEGORY;

    private final RenderSystem sys;
    private final EcsManager ecs;

    public MeshCollector(RenderSystem sys) {
        this.sys = sys;
        this.ecs = sys.service(Services.ECS);

        this.CATEGORY = ecs.createCategory();
        ecs.registerQuery(CATEGORY, new StaticMeshQuery());
    }

    private class StaticMeshQuery implements Query {

        @Override
        public ComponentMask getMask() {
            return ComponentMask.of(StaticMeshC.ID);
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {
            
        }
    }

}
