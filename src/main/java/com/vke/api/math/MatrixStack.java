package com.vke.api.math;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayDeque;
import java.util.Stack;

public class MatrixStack {
    private final Stack<MatrixStack> popped;
    private final ArrayDeque<Matrix4f> stack;

    public MatrixStack() {
        popped = new Stack<>();
        stack = new ArrayDeque<>();
    }

    public void push() {
        push(new Matrix4f());
    }

    public void push(Matrix4f mat) {
        stack.addLast(mat);
    }

    public Matrix4f peek() {
        return stack.peek();
    }

    public void pop() {
        popped.push(this.copy());
        stack.removeLast();
    }

    public MatrixStack copy() {
        MatrixStack copy = new MatrixStack();
        copy.stack.addAll(this.stack);
        return copy;
    }

    public Matrix4f compute() {
        Matrix4f result = stack.removeFirst();
        for (Matrix4f mat : stack) {
            result = result.mul(mat);
        }

        return result;
    }

    public void translate(float x, float y, float z) {
        stack.peek().translate(x, y, z);
    }

    public void scale(float x, float y, float z) {
        stack.peek().scale(x, y, z);
    }

    public void rotate(float xRad, float yRad, float zRad) {
        stack.peek().rotateZYX(zRad, yRad, xRad);
    }

    public @Nullable Matrix4f current() {
        return stack.peek();
    }
}
