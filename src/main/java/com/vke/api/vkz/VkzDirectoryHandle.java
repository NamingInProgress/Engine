package com.vke.api.vkz;

import java.util.Iterator;

public interface VkzDirectoryHandle {
    Iterator<VkzFileHandle> iterateFiles();

    VkzFileHandle file(CharSequence name);

    VkzDirectoryHandle subDir(CharSequence name);
}
