package com.vke.api.rendering.abstraction.draw;

import com.vke.api.framable.Framable;
import com.vke.utils.io.Disposable;

public interface VertexConsumerProvider extends Disposable, Framable {

    <T extends Vertex> VertexConsumer<T> get(T template);
    <T extends Vertex> VertexConsumer<T> get(T template, int estVertexCount, int estIndexCount);

}
