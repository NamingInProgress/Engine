package com.vke.core.vkz.types.lo;

import com.vke.api.serializer.Loader;
import com.vke.api.vkz.ProgressReport;
import com.vke.api.vkz.VkzArchive;
import com.vke.api.vkz.VkzDirectoryHandle;
import com.vke.api.vkz.VkzFileHandle;
import com.vke.core.vkz.VkzObjLoader;
import com.vke.core.vkz.VkzPath;
import com.vke.core.vkz.types.VkzArray;
import com.vke.utils.collection.ArrayIter;
import com.vke.utils.iter.Iter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

public class VkzListOnlyArchive implements VkzArchive {
    private final int magic;

    private final VkzListOnlyDirLayer root;
    private final VkzArray<Integer> fileLengths;
    private final VkzListOnlyFile[] files;

    VkzListOnlyFile[] files() {
        return files;
    }

    public VkzListOnlyArchive(Loader loader) {
        try {
            magic = loader.loadInt();
            root = new VkzListOnlyDirLayer(loader);

            fileLengths = new VkzArray<>(Integer.class, new Integer[0]);
            fileLengths.load(loader);

            int fileCount = fileLengths.length();
            files = new VkzListOnlyFile[fileCount];
            for (int i = 0; i < fileCount; i++) {
                files[i] = new VkzListOnlyFile(fileLengths.elements()[i], loader);
            }
            root.setArchive(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public VkzFileHandle file(CharSequence path) {
        VkzPath p = new VkzPath(path);
        VkzDirectoryHandle layer = root;
        for (int i = 0; i < p.getLength(); i++) {
            String part = p.getPart(i);
            if (p.isLast(i)) {
                return layer.file(part);
            }
            layer = layer.subDir(part);
            if (layer == null) {
                return null;
            }
        }
        return null;
    }

    @Override
    public VkzDirectoryHandle directory(CharSequence path) {
        VkzPath p = new VkzPath(path);
        VkzDirectoryHandle layer = root;
        for (int i = 0; i < p.getLength(); i++) {
            String part = p.getPart(i);
            if (p.isLast(i)) {
                return layer;
            }
            layer = layer.subDir(part);
            if (layer == null) {
                return null;
            }
        }
        return null;
    }

    @Override
    public VkzDirectoryHandle root() {
        return root;
    }

    @Override
    public Iter<VkzFileHandle> iterateFiles() {
        return Iter.of(files);
    }

    @Override
    public void writeOut(OutputStream stream, ProgressReport.Listener progressListener) throws IOException {
        throw new UnsupportedOperationException("Cannot write a ReadOnly vkz archive!");
    }
}
