package com.vke.api.vkz;

import com.vke.api.serializer.Serializer;
import com.vke.core.vkz.VkzObjLoader;
import com.vke.core.vkz.types.imm.VkzImmediateArchive;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public interface VkzArchive {
    static VkzArchive open(InputStream stream, OpenStrategy strategy) throws VkzOpenException {
        VkzObjLoader loader = new VkzObjLoader(stream, 5, 3);

        VkzArchive archive;
        if (strategy == OpenStrategy.LazyFiles) {
            throw new VkzOpenException("Currently no support for LazyFiles sadly :(");
        } else if (strategy == OpenStrategy.OpenAllFiles) {
            archive =  Serializer.loadObject(VkzImmediateArchive.class, loader);
        } else {
            throw new VkzOpenException("Strategy " + strategy + " is illegal!");
        }

        try {
            stream.close();
        } catch (IOException e) {
            throw new VkzOpenException(e);
        }

        return archive;
    }

    VkzFileHandle file(CharSequence path);

    VkzDirectoryHandle directory(CharSequence path);

    VkzDirectoryHandle root();

    Iterator<VkzFileHandle> iterateFiles();
}
