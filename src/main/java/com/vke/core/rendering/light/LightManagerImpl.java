package com.vke.core.rendering.light;

import com.vke.api.rendering.abstraction.light.LightManager;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldResource;
import com.vke.core.color.Color;
import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.services2.Services;
import com.vke.impl.debug.DebugContext;
import com.vke.impl.ecs.TransformC;
import com.vke.impl.ecs.light.PointLightC;
import com.vke.impl.ecs.light.SpotLightC;
import org.joml.Vector3f;

public class LightManagerImpl implements LightManager {

    private final RenderSystem sys;
    private final EcsManager ecs;
    private final int LIGHTS_CATEGORY;

    private FieldArrayResource buf;
    private int counter = 0;

    public LightManagerImpl(RenderSystem sys) {
        this.sys = sys;
        this.ecs = sys.service(Services.ECS);
        LIGHTS_CATEGORY = ecs.createCategory();
        ecs.registerQuery(LIGHTS_CATEGORY, new PointLightsQuery());
        ecs.registerQuery(LIGHTS_CATEGORY, new SpotLightsQuery());
    }

    @Override
    public void write(FieldArrayResource buf, FieldResource lightCount) {
        this.counter = 0;
        this.buf = buf;
        long count = ecs.runQueries(LIGHTS_CATEGORY);
        lightCount.write(writer -> writer.int1((int) count));
    }

    private class SpotLightsQuery implements Query {

        @Override
        public ComponentMask getMask() {
            return new ComponentMask(SpotLightC.ID, TransformC.ID);
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {
            SpotLightC sl = at.getComponentById(SpotLightC.ID);
            TransformC tf = at.getComponentById(TransformC.ID);

            /*
            for (int i = i0; i < i1; i++) {
                float x = tf.x[i], y = tf.y[i], z = tf.z[i];
                Vector3f rot = tf.forward(i);
                float r = sl.r[i], g = sl.g[i], b = sl.b[i];
                float range = sl.range[i];
                float inte = sl.intensity[i];
                float ica = sl.innerConeCos[i], oca = sl.outerConeCos[i];

                //y += (float) (20 * Math.sin(System.nanoTime() / 1_000_000_000.0));
                DebugContext.boundingBox(new Vector3f(x - 1, y - 1, z - 1), new Vector3f(x + 1, y + 1, z + 1),
                        Color.RED);

                float finalY = y;
                buf.write(counter++, writer -> {
                    writer.float4(x, finalY, z, range);
                    writer.float4(r, g, b, inte);
                    writer.float4(rot.x, rot.y, rot.z, 1);

                    writer.float4(0, 0, 0, ica);
                    writer.float4(0 ,0, 0, oca);
                    putLightFlag(writer, 1);
                });
            }
            */
        }
    }

    private class PointLightsQuery implements Query {

        @Override
        public ComponentMask getMask() {
            return new ComponentMask(PointLightC.ID, TransformC.ID);
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {
            PointLightC pl = at.getComponentById(PointLightC.ID);
            TransformC tf = at.getComponentById(TransformC.ID);
            for (int i = i0; i < i1; i++) {
                float x = tf.x[i], y = tf.y[i], z = tf.z[i];
                float r = pl.r[i], g = pl.g[i], b = pl.b[i];
                float range = pl.range[i];
                float inte = pl.intensity[i];

                if (x == 0) {
                    y += (float) (20 * Math.max(Math.sin(System.nanoTime() / 1_000_000_000.0), 0.0));
                }

                float finalY = y;
                buf.write(counter++, writer -> {
                    writer.float4(x, finalY, z, range);
                    writer.float4(r, g, b, inte);
                    putLightFlag(writer, 0);
                });
            }
        }
    }

    private static void putLightFlag(BufferSlice writer, int flag) {
        writer.int1At(44, flag);
    }
}
