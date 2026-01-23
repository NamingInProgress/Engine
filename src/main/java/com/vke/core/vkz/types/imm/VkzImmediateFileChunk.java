package com.vke.core.vkz.types.imm;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.api.vkz.VkzEditor;
import com.vke.api.vkz.VkzFileHandle;
import com.vke.core.vkz.types.VkzName;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class VkzImmediateFileChunk implements VkzFileHandle {
    private VkzName name;
    private byte[] data;
    private final int length;

    public VkzImmediateFileChunk(int length) {
        this.length = length;
        this.name = null;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public String getName() {
        return name != null ? name.getName() : null;
    }

    @Override
    public int getSize() {
        return length;
    }

    @Override
    public VkzEditor edit() {
        return null;
    }

    void save(Saver saver) throws SaveException {
        Serializer.saveObject(name, saver);
        for (byte b : data) saver.saveByte(b);
    }

    void load(Loader loader) throws LoadException {
        name = Serializer.loadObject(VkzName.class, loader);
        data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = loader.loadByte();
        }
    }
}
