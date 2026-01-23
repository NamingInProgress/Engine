package com.vke.core.vkz.types.imm;

import com.carrotsearch.hppc.IntHashSet;
import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.api.vkz.VkzArchive;
import com.vke.api.vkz.VkzDirectoryHandle;
import com.vke.api.vkz.VkzFileHandle;
import com.vke.core.vkz.VkzObjLoader;
import com.vke.core.vkz.VkzPath;
import com.vke.core.vkz.types.VkzArray;
import com.vke.core.vkz.types.VkzDirLayer;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

public class VkzImmediateArchive implements Serializer<VkzImmediateArchive>, VkzArchive {
    public static VkzImmediateArchive SERIALIZER = new VkzImmediateArchive();
    private static final int LOCK_ALL = 0;

    private int magic;
    private long fullBytes;
    private int extraBits;
    private VkzDirLayer root;
    private VkzArray<Integer> fileLengths;
    private VkzImmediateFileChunk[] fileChunks;

    private final ArrayList<EditTask> laterTasks = new ArrayList<>();


    @Override
    public Class<?> getObjectClass() {
        return VkzImmediateArchive.class;
    }

    @Override
    public void save(VkzImmediateArchive value, Saver saver) throws SaveException {
        saver.saveInt(value.magic);
        saver.saveLong(value.fullBytes);
        saver.saveBits(3, value.extraBits);
        Serializer.saveObject(root, saver);
        fileLengths.save(saver);
        for (VkzImmediateFileChunk fileChunk : fileChunks) {
            fileChunk.save(saver);
        }
    }

    @Override
    public VkzImmediateArchive load(Loader loader) throws LoadException {
        if (!(loader instanceof VkzObjLoader actualLoader)) {
            throw new LoadException("Illegal loader used! Please use " + VkzObjLoader.class.getName());
        }

        int magic = loader.loadInt();
        long fullBytes = loader.loadLong();
        int extraBits = loader.loadBits(3);
        actualLoader.setFullBytes(fullBytes);
        actualLoader.setExtraBits(extraBits);
        VkzDirLayer root = Serializer.loadObject(VkzDirLayer.class, loader);
        if (root == null) {
            throw new LoadException("Unable to load filesystem!");
        }
        VkzArray<Integer> fileLengths = new VkzArray<>(Integer.class, new Integer[0]);
        fileLengths.load(loader);
        VkzImmediateFileChunk[] fileChunks = new VkzImmediateFileChunk[fileLengths.length()];
        for (int i = 0; i < fileLengths.length(); i++) {
            VkzImmediateFileChunk chunk = new VkzImmediateFileChunk(fileLengths.elements()[i]);
            chunk.load(loader);
            fileChunks[i] = chunk;
        }

        root.setFilesSource(fileChunks);

        VkzImmediateArchive archive = new VkzImmediateArchive();
        archive.magic = magic;
        archive.fullBytes = fullBytes;
        archive.extraBits = extraBits;
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

    private record EditTask(VkzImmediateEditor.EditedPacket packet, VkzImmediateFileChunk chunk) {
    }
}
