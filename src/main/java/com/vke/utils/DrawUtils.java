package com.vke.utils;

import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.core.color.RgbColor;
import org.joml.Vector3f;

public class DrawUtils {

    public static void fullscreenTri(CommandBuffer cmd) {
        cmd.draw(3, 1, 0, 0);
    }

    public static <T extends Vertex> void tri(VertexConsumer<T> vc, DrawUtilsVF<T> f, Vector3f a, Vector3f b, Vector3f c, RgbColor col) {
        vc.begin();
        vc.vertices(f.apply(a, col), f.apply(b, col), f.apply(c, col));
        vc.indices(0, 1, 2);
    }

    public static <T extends Vertex> void quad(VertexConsumer<T> vc, DrawUtilsVF<T> f, Vector3f a, Vector3f b, RgbColor col) {
        Vector3f c = new Vector3f(a.x, b.y, a.z);
        Vector3f d = new Vector3f(b.x, a.y, b.z);
        tri(vc, f, a, b, c, col);
        tri(vc, f, a, b, d, col);
    }

    @FunctionalInterface
    public interface DrawUtilsVF<T extends Vertex> {
        T apply(Vector3f v, RgbColor c);
    }

}
