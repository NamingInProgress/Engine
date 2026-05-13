package com.vke.core.ui.rendering.core;

public abstract class DrawRequest {
    private Type type;
    private int transform, clip;
    private int texture;

    /// -1 means null
    public DrawRequest(Type type, int transform, int clip, int texture) {
        this.type = type;
        this.transform = transform;
        this.clip = clip;
        this.texture = texture;
    }

    public Type getType() {
        return type;
    }

    public int getTransform() {
        return transform;
    }

    public int getClip() {
        return clip;
    }

    public int getTexture() {
        return texture;
    }

    public enum Type {
        RoundRect,
        Text,
        General
    }
}
