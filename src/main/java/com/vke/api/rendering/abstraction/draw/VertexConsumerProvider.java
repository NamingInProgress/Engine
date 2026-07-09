package com.vke.api.rendering.abstraction.draw;

import com.vke.api.draw.Vertex;
import com.vke.api.draw.VertexConsumer;

public interface VertexConsumerProvider {

    <T extends Vertex> VertexConsumer<T> get(T template);
    <T extends Vertex> VertexConsumer<T> get(T template, int estVertexCount, int estIndexCount);

}
