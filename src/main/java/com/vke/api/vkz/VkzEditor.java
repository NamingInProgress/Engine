package com.vke.api.vkz;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface VkzEditor {
    void clear();

    default void write(CharSequence text) {
        write(text, StandardCharsets.UTF_8);
    }
    default void write(CharSequence text, Charset charset) {
        write(text.toString().getBytes(charset));
    }
    default void write(byte b) {
        write(new byte[] { b });
    }
    default void write(byte[] data) {
        write(data, 0, data.length - 1);
    }
    void write(byte[] data, int start, int endInclusive);

    void commit();
}
