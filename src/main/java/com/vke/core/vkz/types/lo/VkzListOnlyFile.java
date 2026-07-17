package com.vke.core.vkz.types.lo;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Serializer;
import com.vke.api.vkz.VkzEditor;
import com.vke.api.vkz.VkzFileHandle;
import com.vke.core.vkz.types.VkzName;

import java.io.InputStream;

public class VkzListOnlyFile implements VkzFileHandle {
    private final VkzName name;
    private final int length;

    public VkzListOnlyFile(int length, Loader loader) {
        try {
            this.length = length;
            name = Serializer.loadObject(VkzName.class, loader, false);
            //skip data bytes
            loader.loadRaw(length);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getInputStream() {
        throw new UnsupportedOperationException("Cannot get input stream from ListOnly vkz file!");
    }

    @Override
    public String getName() {
        return name.getName();
    }

    @Override
    public int getSize() {
        return length;
    }

    @Override
    public VkzEditor edit() {
        throw new UnsupportedOperationException("Cannot edit ListOnly vkz file!");
    }

    @Override
    public boolean isLocked() {
        return false;
    }
}
