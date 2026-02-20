package com.vke.api.vkz;

import com.vke.api.utils.NotifyingIterable;
import com.vke.utils.iter.Iter;

import java.util.Iterator;

public interface VkzDirectoryHandle {
    String getName();

    Iter<VkzFileHandle> iterateFiles();

    Iter<VkzDirectoryHandle> iterateDirectories();

    VkzFileHandle file(CharSequence name);

    VkzDirectoryHandle subDir(CharSequence name);

    VkzFileHandle createFile(CharSequence name);

    VkzDirectoryHandle createDirectory(CharSequence name);

    void deleteFile(CharSequence name);

    void deleteDirectory(CharSequence name);
}
