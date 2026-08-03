package com.vke.core.rendering.transform;

import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.rendering.vertexconsumer.RecyclerArrayList;
import org.joml.Matrix4f;

import java.util.Arrays;

public class MatrixStack {
    private final RecyclerArrayList<Matrix4f> stack;
    private int[] indexStack;   // Maps logical stack depth -> physical list index
    private int depth;          // Current logical stack depth
    private int nextListIndex;  // Next available physical slot in the list
    private Matrix4f m;

    public MatrixStack() {
        this(10);
    }

    public MatrixStack(int capacity) {
        stack = new RecyclerArrayList<>(capacity);
        indexStack = new int[capacity];

        this.m = new Matrix4f();
        stack.add(m);

        indexStack[0] = 0;
        depth = 0;
        nextListIndex = 1;
    }

    public void reset() {
        stack.clear();
        stack.virtualAdd();

        depth = 0;
        nextListIndex = 1;
        indexStack[0] = 0;

        this.m = stack.get(0).identity();
    }

    public void push() {
        if (depth + 1 >= indexStack.length) {
            indexStack = Arrays.copyOf(indexStack, indexStack.length * 2);
        }

        Matrix4f current = m;
        int newPhysicalIndex = nextListIndex++;

        Matrix4f next = stack.getOrCreateElement(newPhysicalIndex, true, Matrix4f::new);
        next.set(current);

        depth++;
        indexStack[depth] = newPhysicalIndex;
        m = next;
    }

    public void pop() {
        if (depth > 0) {
            depth--;
            m = stack.get(indexStack[depth]);
        }
    }

    public Matrix4f current() {
        return m;
    }

    public int currentMatrixIndex() {
        return indexStack[depth];
    }

    public int size() {
        return depth + 1;
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
        for (int i = 0; i < nextListIndex; i++) {
            sink.putMat4(stack.get(i));
        }
    }
}
