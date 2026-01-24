package com.vke.core.vkz.types;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

import java.nio.charset.StandardCharsets;

public class VkzName implements Serializer<VkzName> {
    static VkzName SERIALIZER = new VkzName("");

    private String name;

    public VkzName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Class<?> getObjectClass() {
        return VkzName.class;
    }

    @Override
    public void save(VkzName value, Saver saver) throws SaveException {
        byte[] utf8 = value.name.getBytes(StandardCharsets.UTF_8);

        if (utf8.length > 255) {
            throw new SaveException("Name too long (max 255 UTF-8 bytes)");
        }

        saver.saveByte((byte) utf8.length);
        for (byte b : utf8) {
            saver.saveByte(b);
        }
    }

    @Override
    public VkzName load(Loader loader) throws LoadException {
        int length = loader.loadByte() & 0xFF; // uint8

        byte[] utf8 = new byte[length];
        for (int i = 0; i < length; i++) {
            utf8[i] = loader.loadByte();
        }

        VkzName name = new VkzName("");
        name.name = new String(utf8, StandardCharsets.UTF_8);
        return name;
    }
}
