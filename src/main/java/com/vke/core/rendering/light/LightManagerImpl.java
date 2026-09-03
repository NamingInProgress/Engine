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
import com.vke.impl.ecs.WorldTransformC;
import com.vke.impl.rendering.debug.DebugContext;
import com.vke.impl.ecs.TransformC;
import com.vke.impl.ecs.light.DirectionalLightC;
import com.vke.impl.ecs.light.PointLightC;
import com.vke.impl.ecs.light.SpotLightC;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

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
        lightCount.write(writer -> writer.int1((int) count));
    }

    private class DirectionalLightsQuery implements Query {

        private static final Vector4f forward = new Vector4f();
        private static final Vector3f pos = new Vector3f();

        @Override
        public ComponentMask getMask() {
            return new ComponentMask(DirectionalLightC.ID, TransformC.ID);
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {
            DirectionalLightC sl = at.getComponentById(DirectionalLightC.ID);
            WorldTransformC wtc = at.getComponentById(WorldTransformC.ID);

            for (int i = i0; i < i1; i++) {
                wtc.getWorldPosition(i, pos);
                wtc.getForward(i, forward);

                float r = sl.r[i], g = sl.g[i], b = sl.b[i];
                float inte = sl.intensity[i];

                DebugContext.arrow(new Vector3f(pos.x, pos.y, pos.z), new Vector3f(forward.x, forward.y, forward.z), RgbColor.RED);
                DebugContext.boundingBox(new Vector3f(pos.x - 1, pos.y - 1, pos.z - 1),
                        new Vector3f(pos.x + 1, pos.y + 1, pos.z + 1), RgbColor.RED);

                buf.write(counter++, writer -> {
                    writer.float4(0, 0, 0, 0); // POS + RANGE
                    writer.float4(r, g, b, inte); // COLOR + INTENSITY
                    writer.float4(forward.x, forward.y, forward.z, DirectionalLightC.TYPE); // DIRECTION + LIGHT TYPE
                    putLightFlag(writer, DirectionalLightC.TYPE);
                });
            }
        }
    }

    private class SpotLightsQuery implements Query {

        final static private Vector4f forward = new Vector4f();
        final static private Vector3f pos = new Vector3f();

        @Override
        public ComponentMask getMask() {
            return new ComponentMask(SpotLightC.ID, TransformC.ID);
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {
            SpotLightC sl = at.getComponentById(SpotLightC.ID);
            WorldTransformC wtc = at.getComponentById(WorldTransformC.ID);

            for (int i = i0; i < i1; i++) {
                wtc.getWorldPosition(i, pos);
                wtc.getForward(i, forward);

                float r = sl.r[i], g = sl.g[i], b = sl.b[i];
                float range = sl.range[i];
                float inte = sl.intensity[i];
                float ica = sl.innerConeCos[i], oca = sl.outerConeCos[i];

                DebugContext.boundingBox(new Vector3f(pos.x - 1, pos.y - 1, pos.z - 1), new Vector3f(pos.x + 1, pos.y + 1, pos.z + 1),
                        RgbColor.RED);
//                DebugContext.arrow(new Vector3f(pos.x, pos.y, pos.z), new Vector3f(forward.x, forward.y, forward.z), RgbColor.GREEN);
                buf.write(counter++, writer -> {
                    writer.float4(pos.x, pos.y, pos.z, range);
                    writer.float4(r, g, b, inte);
                    writer.float4(forward.x, forward.y, forward.z, SpotLightC.TYPE);

                    writer.float4(0, 0, 0, ica);
                    writer.float4(0 ,0, 0, oca);
                    putLightFlag(writer, SpotLightC.TYPE);
                });
            }
        }
    }

    private class PointLightsQuery implements Query {

        private static final Vector3f pos = new Vector3f();

        @Override
        public ComponentMask getMask() {
            return new ComponentMask(PointLightC.ID, TransformC.ID);
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {
            PointLightC pl = at.getComponentById(PointLightC.ID);
            WorldTransformC wtc = at.getComponentById(WorldTransformC.ID);
            for (int i = i0; i < i1; i++) {
                wtc.getWorldPosition(i, pos);
                float r = pl.r[i], g = pl.g[i], b = pl.b[i];
                float range = pl.range[i];
                float inte = pl.intensity[i];

                buf.write(counter++, writer -> {
                    writer.float4(pos.x, pos.y, pos.z, range);
                    writer.float4(r, g, b, inte);
                    putLightFlag(writer, PointLightC.TYPE);
                });
            }
        }
    }

    private static void putLightFlag(BufferSlice writer, int flag) {
        writer.int1At(44, flag);
    }
}
