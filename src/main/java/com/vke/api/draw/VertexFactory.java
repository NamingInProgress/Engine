package com.vke.api.draw;

import com.vke.api.draw.shape.ShapeRendererVertex;
import com.vke.api.rendering.abstraction.data.Texture;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface VertexFactory<T extends Vertex> {

    VertexFactory<ShapeRendererVertex> DEFAULT = ShapeRendererVertex::new;

    T apply(float x, float y, float z, float r, float g, float b, float a, float u, float v, @Nullable Texture texture);

}
