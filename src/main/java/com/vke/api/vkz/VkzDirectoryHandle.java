package com.vke.api.vkz;

import com.vke.api.utils.NotifyingIterable;

public interface VkzDirectoryHandle {
    NotifyingIterable<VkzFileHandle> iterateFiles();

    VkzFileHandle file(CharSequence name);

    VkzDirectoryHandle subDir(CharSequence name);
}
