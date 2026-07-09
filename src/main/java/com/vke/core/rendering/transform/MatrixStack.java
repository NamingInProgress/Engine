package com.vke.core.rendering.transform;

import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.rendering.vertexconsumer.RecyclerArrayList;
import org.joml.Matrix4f;

public class MatrixStack {
    private final RecyclerArrayList<Matrix4f> stack;
    private int index;
    private Matrix4f m;

    public MatrixStack() {
        this(10);
    }

    public MatrixStack(int capacity) {
        stack = new RecyclerArrayList<>(capacity);

        this.m = new Matrix4f();
        stack.add(m);

        index = 1;
    }

    public void reset() {
        index = 1;
        stack.clear();
        stack.virtualAdd();

        this.m = stack.get(0).identity();
    }

    public void push() {
        Matrix4f current = stack.get(index - 1);
        Matrix4f next = stack.getOrCreateElement(index, true, Matrix4f::new);

        next.set(current);
        m = next;
        index++;
    }

    public void pop() {
        if (index > 1) {
            index--;
        }
        m = stack.get(index - 1);
    }

    public Matrix4f current() {
        return m;
    }

    public int currentMatrixIndex() {
        return index - 1;
    }

    public int size() {
        return index;
    }

    public void transform(LinearTransform transform) {
        m.mul(transform.matrix());
    }

    public void translate(float x, float y, float z) {
        m.translate(x, y, z);
    }

    public void translate(float x, float y) {
        m.translate(x, y, 0);
    }

    public void rotate(float x, float y, float z) {
        m.rotateXYZ(x, y, z);
    }

    public void rotate(float z) {
        m.rotateZ(z);
    }

    public void scale(float s) {
        m.scale(s);
    }

    public void scale(float x, float y, float z) {
        m.scale(x, y, z);
    }

    public void shear(float x, float y, float z) {
        transform(new Shear(x, y, z));
    }

    public void upload(BufferSlice sink) {
        for (int i = 0, l = stack.len(); i < l; i++) {
            sink.putMat4(stack.get(i));
        }
    }
}
