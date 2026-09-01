package com.vke.core.rendering.light;

import com.vke.api.rendering.abstraction.light.LightManager;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldResource;
import com.vke.core.color.RgbColor;
import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.services2.Services;
import com.vke.impl.rendering.debug.DebugContext;
import com.vke.impl.ecs.TransformC;
import com.vke.impl.ecs.light.DirectionalLightC;
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
        ecs.registerQuery(LIGHTS_CATEGORY, new DirectionalLightsQuery());
    }

    @Override
    public void write(FieldArrayResource buf, FieldResource lightCount) {
        this.counter = 0;
        this.buf = buf;
        long count = ecs.runQueries(LIGHTS_CATEGORY);
        lightCount.write(writer -> writer.putInt((int) count));
    }

    private class DirectionalLightsQuery implements Query {
        @Override
        public ComponentMask getMask() {
            return new ComponentMask(DirectionalLightC.ID, TransformC.ID);
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {
            DirectionalLightC sl = at.getComponentById(DirectionalLightC.ID);
            TransformC tf = at.getComponentById(TransformC.ID);
            for (int i = i0; i < i1; i++) {
                float x = tf.x[i], y = tf.y[i], z = tf.z[i];
                Vector3f rot = tf.forward(i);
                float r = sl.r[i], g = sl.g[i], b = sl.b[i];
                float inte = sl.intensity[i];

                DebugContext.arrow(new Vector3f(x, y, z), new Vector3f(rot), RgbColor.RED);
                DebugContext.boundingBox(new Vector3f(x - 1, y - 1, z - 1), new Vector3f(x + 1, y + 1, z + 1),
                        RgbColor.RED);

                buf.write(counter++, writer -> {
                    writer.putFloat4(0, 0, 0, 0); // POS + RANGE
                    writer.putFloat4(r, g, b, inte); // COLOR + INTENSITY
                    writer.putFloat4(rot.x, rot.y, rot.z, DirectionalLightC.TYPE); // DIRECTION + LIGHT TYPE
                    putLightFlag(writer, DirectionalLightC.TYPE);
                });
            }
        }
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
            for (int i = i0; i < i1; i++) {
                float x = tf.x[i], y = tf.y[i], z = tf.z[i];
                Vector3f rot = tf.forward(i);
                float r = sl.r[i], g = sl.g[i], b = sl.b[i];
                float range = sl.range[i];
                float inte = sl.intensity[i];
                float ica = sl.innerConeCos[i], oca = sl.outerConeCos[i];

//                DebugContext.boundingBox(new Vector3f(x - 1, y - 1, z - 1), new Vector3f(x + 1, y + 1, z + 1),
//                        RgbColor.RED);

                buf.write(counter++, writer -> {
                    writer.putFloat4(x, y, z, range);
                    writer.putFloat4(r, g, b, inte);
                    writer.putFloat4(rot.x, rot.y, rot.z, SpotLightC.TYPE);

                    writer.putFloat4(0, 0, 0, ica);
                    writer.putFloat4(0 ,0, 0, oca);
                    putLightFlag(writer, SpotLightC.TYPE);
                });
            }
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

                buf.write(counter++, writer -> {
                    writer.putFloat4(x, y, z, range);
                    writer.putFloat4(r, g, b, inte);
                    putLightFlag(writer, PointLightC.TYPE);
                });
            }
        }
    }

    private static void putLightFlag(BufferSlice writer, int flag) {
        writer.putIntAt(44, flag);
    }
}
