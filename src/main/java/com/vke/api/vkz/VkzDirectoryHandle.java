package com.vke.api.vkz;

import com.vke.api.utils.NotifyingIterable;

import java.util.Iterator;

public interface VkzDirectoryHandle {
    String getName();

    Iterator<VkzFileHandle> iterateFiles();

    Iterator<VkzDirectoryHandle> iterateDirectories();

    VkzFileHandle file(CharSequence name);

    VkzDirectoryHandle subDir(CharSequence name);

    VkzFileHandle createFile(CharSequence name);

    VkzDirectoryHandle createDirectory(CharSequence name);

    void deleteFile(CharSequence name);

    void deleteDirectory(CharSequence name);
}
