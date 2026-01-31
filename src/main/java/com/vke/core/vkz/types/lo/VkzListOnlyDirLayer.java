package com.vke.core.vkz.types.lo;

import com.vke.api.vkz.VkzDirectoryHandle;
import com.vke.api.vkz.VkzFileHandle;
import com.vke.core.vkz.types.VkzArray;
import com.vke.core.vkz.types.VkzName;

import java.util.Iterator;

public class VkzListOnlyDirLayer implements VkzDirectoryHandle {
    private VkzName name;

    private VkzListOnlyDirLayer subLayers;
    private VkzArray<Integer> fileOffsets;

    @Override
    public String getName() {
        return name.getName();
    }

    @Override
    public Iterator<VkzFileHandle> iterateFiles() {
        return null;
    }

    @Override
    public Iterator<VkzDirectoryHandle> iterateDirectories() {
        return null;
    }

    @Override
    public VkzFileHandle file(CharSequence name) {
        return null;
    }

    @Override
    public VkzDirectoryHandle subDir(CharSequence name) {
        return null;
    }

    @Override
    public VkzFileHandle createFile(CharSequence name) {
        return null;
    }

    @Override
    public VkzDirectoryHandle createDirectory(CharSequence name) {
        return null;
    }

    @Override
    public void deleteFile(CharSequence name) {

    }

    @Override
    public void deleteDirectory(CharSequence name) {

    }
}
