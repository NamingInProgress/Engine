package com.vke.core.rendering.transform;

import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.vulkan.vertexconsumer.RecyclerArrayList;
import org.joml.Matrix4f;

public class MatrixStack {
    private final RecyclerArrayList<Matrix4f> stack;
    private int index;

    public MatrixStack() {
        this(10);
    }

    public MatrixStack(int capacity) {
        stack = new RecyclerArrayList<>(capacity);

        Matrix4f identity = new Matrix4f();
        stack.add(identity);

        index = 1;
    }

    public void reset() {
        index = 1;
        stack.clear();
        stack.virtualAdd();

        stack.get(0).identity();
    }

    public void push() {
        Matrix4f current = stack.get(index - 1);
        Matrix4f next = stack.getOrCreateElement(true, Matrix4f::new);

        next.set(current);
        index++;
    }

    public void pop() {
        if (index > 1) {
            index--;
        }
    }

    public Matrix4f current() {
        return stack.get(index - 1);
    }

    public int currentMatrixIndex() {
        return index - 1;
    }

    public int size() {
        return index;
    }

    public void translate(float x, float y, float z) {
        current().translate(x, y, z);
    }

    public void translate(float x, float y) {
        current().translate(x, y, 0);
    }

    public void rotate(float x, float y, float z) {
        current().rotateXYZ(x, y, z);
    }

    public void rotate(float z) {
        current().rotateZ(z);
    }

    public void scale(float s) {
        current().scale(s);
    }

    public void scale(float x, float y, float z) {
        current().scale(x, y, z);
    }

    public void shear(float x, float y, float z) {
        // however you implement shear
    }

    public void upload(BufferSlice sink) {
        for (int i = 0, l = stack.len(); i < l; i++) {
            sink.putMat4(stack.get(i));
        }
    }
}
