package com.vke.api.vkz;

/**
 * The way the implementation handles the loading of the archive.
 */
public enum ArchiveType {
    /**
     * File structure gets loaded immediately, but the contents are not yet queried. If a file inputStream or a VkzEditor is acquired,
     * the files contents get read from disk.
     */
    LazyFiles,

    /**
     * All files of this archive get loaded immediately including their contents.
     */
    InflateAll,

    /**
     * This is a read only implementation which only shows the file structure. Contents can not be acquired.
     */
    ListOnly
}
