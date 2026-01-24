package com.vke.core.vkz.types.imm;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.api.vkz.VkzArchive;
import com.vke.api.vkz.VkzDirectoryHandle;
import com.vke.api.vkz.VkzFileHandle;
import com.vke.core.vkz.VkzObjLoader;
import com.vke.core.vkz.VkzObjSaver;
import com.vke.core.vkz.VkzPath;
import com.vke.core.vkz.types.VkzArray;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class VkzImmediateArchive implements Serializer<VkzImmediateArchive>, VkzArchive {
    public static VkzImmediateArchive SERIALIZER = new VkzImmediateArchive();

    private int magic;
    private VkzImmediateDirLayer root;
    private VkzArray<Integer> fileLengths;
    private VkzImmediateFileChunk[] fileChunks;


    public static VkzImmediateArchive empty() {
        VkzImmediateArchive archive = new VkzImmediateArchive();
        archive.magic = 0x0564b5a30;
        archive.root = VkzImmediateDirLayer.empty("root");
        archive.root.setArchive(archive);
        archive.fileLengths = new VkzArray<>(Integer.class, new Integer[0]);
        archive.fileChunks = new VkzImmediateFileChunk[0];
        return archive;
    }

    @Override
    public Class<?> getObjectClass() {
        return VkzImmediateArchive.class;
    }

    @Override
    public void save(VkzImmediateArchive value, Saver saver) throws SaveException {
        saver.saveInt(value.magic);
        Serializer.saveObject(value.root, saver);
        value.fileLengths.save(saver);
        for (VkzImmediateFileChunk fileChunk : value.fileChunks) {
            fileChunk.save(saver);
        }
    }

    @Override
    public VkzImmediateArchive load(Loader loader) throws LoadException {
        if (!(loader instanceof VkzObjLoader)) {
            throw new LoadException("Illegal loader used! Please use " + VkzObjLoader.class.getName());
        }

        int magic = loader.loadInt();
        VkzImmediateDirLayer root = Serializer.loadObject(VkzImmediateDirLayer.class, loader);
        if (root == null) {
            throw new LoadException("Unable to load filesystem!");
        }
        root.setArchive(this);
        VkzArray<Integer> fileLengths = new VkzArray<>(Integer.class, new Integer[0]);
        fileLengths.load(loader);
        VkzImmediateFileChunk[] fileChunks = new VkzImmediateFileChunk[fileLengths.length()];
        for (int i = 0; i < fileLengths.length(); i++) {
            VkzImmediateFileChunk chunk = new VkzImmediateFileChunk(this, fileLengths.elements()[i], i);
            chunk.load(loader);
            fileChunks[i] = chunk;
        }

        root.setFilesSource(fileChunks);

        VkzImmediateArchive archive = new VkzImmediateArchive();
        archive.magic = magic;
        archive.root = root;
        archive.fileLengths = fileLengths;
        archive.fileChunks = fileChunks;
        return archive;
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
    public Iterator<VkzFileHandle> iterateFiles() {
        return new VkzFileIter(fileChunks);
    }

    @Override
    public void writeOut(OutputStream stream) throws IOException {
        VkzObjSaver saver = new VkzObjSaver(stream);
        Serializer.saveObject(this, saver);
        saver.flush();
        saver.close();
    }

    int insertNewFile(VkzImmediateFileChunk newChunk) {
        int index = fileChunks.length;
        fileChunks = Arrays.copyOf(fileChunks, fileChunks.length + 1);
        fileChunks[index] = newChunk;
        fileLengths.add(newChunk.getSize());
        root.setFilesSource(fileChunks);
        return index;
    }

    void updateLength(int offset, int length) {
        fileLengths.elements()[offset] = length;
    }

    private static class VkzFileIter implements Iterator<VkzFileHandle> {
        private int index;
        private final VkzImmediateFileChunk[] source;

        public VkzFileIter(VkzImmediateFileChunk[] source) {
            this.source = source;
        }

        @Override
        public boolean hasNext() {
            return index < source.length;
        }

        @Override
        public VkzFileHandle next() {
            return source[index++];
        }
    }
}
