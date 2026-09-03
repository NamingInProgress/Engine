package com.vke.impl.rendering.debug;

import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.core.color.RgbColor;
import com.vke.impl.rendering.vertex.DebugVertex;
import com.vke.utils.DrawUtils;
import org.joml.Vector3f;

import java.util.ArrayList;

public class DebugContext {

    private static final DrawUtils.DrawUtilsVF<DebugVertex> FACTORY = (a, b) -> new DebugVertex(a.x, a.y, a.z, b.r(), b.g(), b.b(), b.a());

    static final ArrayList<DebugCommand> tri_commands = new ArrayList<>();
    static final ArrayList<DebugCommand> line_commands = new ArrayList<>();

    public static void arrow(Vector3f position, Vector3f direction, RgbColor color) {
        tri_commands.add(new ArrowCommand(position, direction, color));
    }

    public static void boundingBox(Vector3f a, Vector3f b, RgbColor color) {
        line_commands.add(new BoundingBoxCommand(a, b, color));
    }

    public static void clear() {
        tri_commands.clear();
        line_commands.clear();
    }

    public static abstract class DebugCommand {
        public abstract void draw(VertexConsumer<DebugVertex> vc);
    }

    public static class ArrowCommand extends DebugCommand {
        public final Vector3f pos, dir;
        public final float length;
        public final RgbColor color;

        private VertexConsumer<DebugVertex> vc;

        public ArrowCommand(Vector3f pos, Vector3f dir, RgbColor color) {
            this.pos = pos;
            this.dir = dir;
            this.color = color;
            this.length = dir.length();
        }

        @Override
        public void draw(VertexConsumer<DebugVertex> vc) {
            this.vc = vc;
            Vector3f axis = new Vector3f(dir).normalize();

            float shaftRadius = length * 0.03f;
            float headRadius = shaftRadius * 2.5f;
            float headLength = length * 0.2f;

            Vector3f up = Math.abs(axis.y) < 0.99f
                    ? new Vector3f(0, 1, 0)
                    : new Vector3f(1, 0, 0);

            Vector3f right = up.cross(axis, new Vector3f()).normalize();
            up = axis.cross(right, new Vector3f()).normalize();

            // Tip of the arrow
            Vector3f tip = new Vector3f(pos).fma(length, axis);

            // Where the cone/head starts
            Vector3f headBase = new Vector3f(tip).fma(-headLength, axis);

            // --------------------
            // Shaft
            // --------------------

            Vector3f r = new Vector3f(right).mul(shaftRadius);
            Vector3f u = new Vector3f(up).mul(shaftRadius);

            Vector3f s0 = new Vector3f(pos).add(r).add(u);
            Vector3f s1 = new Vector3f(pos).sub(r).add(u);
            Vector3f s2 = new Vector3f(pos).sub(r).sub(u);
            Vector3f s3 = new Vector3f(pos).add(r).sub(u);

            Vector3f e0 = new Vector3f(headBase).add(r).add(u);
            Vector3f e1 = new Vector3f(headBase).sub(r).add(u);
            Vector3f e2 = new Vector3f(headBase).sub(r).sub(u);
            Vector3f e3 = new Vector3f(headBase).add(r).sub(u);

            quad(s0, s1, e1, e0);
            quad(s1, s2, e2, e1);
            quad(s2, s3, e3, e2);
            quad(s3, s0, e0, e3);

            // Shaft back cap
            quad(s0, s3, s2, s1);

            // --------------------
            // Arrow head
            // --------------------

            Vector3f hr = new Vector3f(right).mul(headRadius);
            Vector3f hu = new Vector3f(up).mul(headRadius);

            Vector3f h0 = new Vector3f(headBase).add(hr).add(hu);
            Vector3f h1 = new Vector3f(headBase).sub(hr).add(hu);
            Vector3f h2 = new Vector3f(headBase).sub(hr).sub(hu);
            Vector3f h3 = new Vector3f(headBase).add(hr).sub(hu);

            // Cone
            tri(h0, h1, tip);
            tri(h1, h2, tip);
            tri(h2, h3, tip);
            tri(h3, h0, tip);

            // Head back cap
            quad(h0, h3, h2, h1);
        }

        void v(Vector3f a) {
            // Fixed: changed second 'a.x' to 'a.z'
            vc.vertices(new DebugVertex(a.x, a.y, a.z, color.r(), color.g(), color.b(), color.a()));
        }

        void tri(Vector3f a, Vector3f b, Vector3f c) {
            vc.begin();
            v(a);
            v(b);
            v(c);
            vc.indices(0, 1, 2);
        }

        void quad(Vector3f a, Vector3f b, Vector3f c, Vector3f d) {
            tri(a, b, c);
            tri(a, c, d);
        }
    }

    public static class BoundingBoxCommand extends DebugCommand {
        private final Vector3f a, b;
        private final RgbColor color;

        public BoundingBoxCommand(Vector3f a, Vector3f b, RgbColor color) {
            this.a = a;
            this.b = b;
            this.color = color;
        }

        @Override
        public void draw(VertexConsumer<DebugVertex> vc) {
            vc.begin();
            v(vc, a);
            v(vc, b);
            v(vc, new Vector3f(a.x, a.y, b.z));
            v(vc, new Vector3f(b.x, a.y, a.z));
            v(vc, new Vector3f(b.x, a.y, b.z));
            v(vc, new Vector3f(a.x, b.y, b.z));
            v(vc, new Vector3f(b.x, b.y, a.z));
            v(vc, new Vector3f(a.x, b.y, a.z));



            line(vc, 0, 2);
            line(vc, 0, 3);
            line(vc, 1, 5);
            line(vc, 1, 6);

            line(vc, 4, 2);
            line(vc, 4, 3);
            line(vc, 7, 5);
            line(vc, 7, 6);

            line(vc, 0, 7);
            line(vc, 2, 5);
            line(vc, 3, 6);
            line(vc, 4, 1);
        }

        void v(VertexConsumer<DebugVertex> vc, Vector3f v) {
            vc.vertices(new DebugVertex(v.x, v.y, v.z, color.r(), color.g(), color.b(), color.a()));
        }

        void line(VertexConsumer<DebugVertex> vc, int idx, int idx2) {
            vc.indices(idx, idx2);
        }
    }

}
