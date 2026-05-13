package com.vke.core.ui.rendering.core;

public class GeneralDrawRequest extends DrawRequest {
    private final UiGeneralVertex[] vertices;
    private final int[] indices;

    public GeneralDrawRequest(int transform, int clip, int texture, UiGeneralVertex[] vertices, int[] indices) {
        super(Type.General, transform, clip, texture);
        this.vertices = vertices;
        this.indices = indices;
    }

    public UiGeneralVertex[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }
}
