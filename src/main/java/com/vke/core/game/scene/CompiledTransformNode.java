package com.vke.core.game.scene;

public class CompiledTransformNode {
    public int worldMatIndex;
    public float[] worldMatrix;

    public int localIndex;
    public float[] x, y, z;
    public float[] ox, oy, oz;
    public float[] rx, ry, rz, rw;
    public float[] sx, sy, sz;

    public int parentNode;
    public int subSize;
    public boolean isDirty;
}
