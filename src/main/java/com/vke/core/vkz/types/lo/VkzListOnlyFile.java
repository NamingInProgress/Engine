package com.vke.core.vkz.types.lo;

import com.vke.api.vkz.VkzEditor;
import com.vke.api.vkz.VkzFileHandle;
import com.vke.core.vkz.types.VkzName;

import java.io.InputStream;

public class VkzListOnlyFile implements VkzFileHandle {
    private VkzName name;
    private int length;

    public VkzListOnlyFile(int length) {
        this.length = length;
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
