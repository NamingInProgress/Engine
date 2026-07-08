package com.vke.core.rendering.transform;

import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.vulkan.vertexconsumer.RecyclerArrayList;
import org.joml.Matrix4f;

public class MatrixStack {
    private final RecyclerArrayList<Matrix4f> stack;
    private int index;
    private Matrix4f m;
    private boolean consecutive;

    public MatrixStack() {
        this(10);
    }

    public MatrixStack(int cap) {
        this.stack = new RecyclerArrayList<>(cap);
        this.m = new Matrix4f();
        this.stack.add(m);
    }

    public void reset() {
        index = 0;
        stack.clear();
        stack.virtualAdd();
        this.m = stack.get(0);
    }

    public void push(LinearTransform transform) {
        if (!consecutive) {
            stack.add(m);
            index++;
        }
        Matrix4f mat = transform.matrix();
        Matrix4f newM = stack.getOrCreateElement(true, Matrix4f::new);
        m.mul(mat, newM);
        m = newM;
    }

    public void pop() {
        index--;
        if (index < 0) index++;
        m = stack.get(index);
    }

    public void begin() {
        consecutive = true;
    }

    public void end() {
        consecutive = false;
        stack.add(m);
        index++;
    }

    public void translate(float x, float y, float z) {
        push(new Translate(x, y, z));
    }

    public void translate(float x, float y) {
        push(new Translate(x, y));
    }

    public void shear(float x, float y, float z) {
        push(new Shear(x, y, z));
    }

    public void rotate(float x, float y, float z) {
        push(new Rotate(x, y, z));
    }

    public void rotate(float z) {
        push(new Rotate(z));
    }

    public void scale(float scale) {
        push(new Scale(scale));
    }

    public void scale(float x, float y, float z) {
        push(new Scale(x, y, z));
    }

    public int len() {
        return stack.len();
    }

    public void upload(BufferSlice sink) {
        for (int i = 0, l = len(); i < l; i++) {
            sink.putMat4(stack.get(i));
        }
    }
}
