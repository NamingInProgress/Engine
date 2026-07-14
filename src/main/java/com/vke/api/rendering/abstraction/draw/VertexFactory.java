package com.vke.api.rendering.abstraction.draw;

import com.vke.api.rendering.abstraction.renderer.data.Texture;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface VertexFactory<T extends Vertex> {

    T apply(float x, float y, float z, float r, float g, float b, float a, float u, float v, int matId, @Nullable Texture texture);

}
