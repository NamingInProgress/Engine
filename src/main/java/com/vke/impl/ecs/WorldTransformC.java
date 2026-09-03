package com.vke.impl.ecs;

import com.vke.core.ecs.component.Component;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import pl.epsi.EcsComponent;
import pl.epsi.Span;

@EcsComponent
public class WorldTransformC implements Component {

    private static Matrix4f wm = new Matrix4f(); // THIS IS NOT THREAD SAFE LIL BRO

    @Span(16)
    public float[] worldMatrix;

    @Override
    public void initialize(int i) {
        System.arraycopy(Inner.ID_MAT, 0, worldMatrix, i, 16);
    }

    @Contract(mutates = "param2")
    public void getMatrix(int i, Matrix4f dest) {
        dest.set(worldMatrix, i * 16);
    }

    private static class Inner {
        private static final float[] ID_MAT = {
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1,
        };
    }

    public Vector4f getForward(int i) {
        Vector4f dest = new Vector4f();
        getForward(i, dest);
        return dest;
    }

    public void getForward(int i, Vector4f forward) {
        forward.set(0, 0, -1, 0);
        getWorldMatrix(i, wm);

        wm.transform(forward);
    }

    public void getWorldMatrix(int i, float[] arr) {
        int index = i * 16;
        System.arraycopy(worldMatrix, index, arr, 0, 16);
    }

    public void getWorldMatrix(int i, Matrix4f dest) {
        dest.set(worldMatrix, i * 16);
    }

    public Vector3f getWorldPosition(int i) {
        Vector3f dest = new Vector3f();
        getWorldPosition(i, dest);
        return dest;
    }

    public void getWorldPosition(int i, Vector3f dest) {
        float[] allMats = worldMatrix;
        int index = i * 16;
        dest.x = allMats[index + 12];
        dest.y = allMats[index + 13];
        dest.z = allMats[index + 14];
    }
}
