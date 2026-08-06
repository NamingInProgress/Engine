package com.vke.core.rendering.light;

import com.vke.api.rendering.abstraction.light.LightManager;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldResource;
import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.services2.Services;
import com.vke.impl.ecs.TransformC;
import com.vke.impl.ecs.light.PointLightC;

public class LightManagerImpl implements LightManager {

    private final RenderSystem sys;
    private final EcsManager ecs;
    private final int LIGHTS_CATEGORY;

    private FieldArrayResource buf;

    public LightManagerImpl(RenderSystem sys) {
        this.sys = sys;
        this.ecs = sys.service(Services.ECS);
        LIGHTS_CATEGORY = ecs.createCategory();
        ecs.registerQuery(LIGHTS_CATEGORY, new PointLightsQuery());
    }

    @Override
    public void write(FieldArrayResource buf, FieldResource lightCount) {
        this.buf = buf;
        long count = ecs.runQueries(LIGHTS_CATEGORY);
        lightCount.write(writer -> writer.putInt((int) count));
    }

    //@EcsQuery(PointLightComponent.ID, TransformComponent.ID)
    private class PointLightsQuery implements Query {

        @Override
        public ComponentMask getMask() {
            return new ComponentMask(PointLightC.ID, TransformC.ID);
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {
            PointLightC pl = at.getComponentById(PointLightC.ID);
            TransformC tf = at.getComponentById(TransformC.ID);
            int counter = 0;
            for (int i = i0; i < i1; i++) {
                float x = tf.x[i], y = tf.y[i], z = tf.z[i];
                float r = pl.r[i], g = pl.g[i], b = pl.b[i];
                float range = pl.range[i];
                float inte = pl.intensity[i];

                buf.write(counter++, writer -> {
                    writer.putFloat4(x, y, z, range);
                    writer.putFloat4(r, g, b, inte);
                });
            }
        }
    }
}
