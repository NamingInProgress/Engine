package com.vke.api.rendering.vulkan.buffer;

public interface ByteSink {
    void b(byte b);
    void b(byte[] b);
    void b(byte[] b, int off, int len);
    void i8(int i);
    void i16(int i);
    void i32(int i);
    void i64(int i);
    void f32(float f);
    void f64(double d);
}
