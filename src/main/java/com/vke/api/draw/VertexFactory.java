package com.vke.api.draw;

import com.vke.core.draw.ShapeRendererVertex;
import com.vke.api.rendering.abstraction.data.Texture;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface VertexFactory<T extends Vertex> {

    T apply(float x, float y, float z, float r, float g, float b, float a, float u, float v, int matId, @Nullable Texture texture);

}
