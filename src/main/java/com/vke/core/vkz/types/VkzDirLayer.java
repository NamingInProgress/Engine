package com.vke.core.vkz.types;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.api.utils.NotifyingIterable;
import com.vke.api.vkz.VkzArchive;
import com.vke.api.vkz.VkzDirectoryHandle;
import com.vke.api.vkz.VkzFileHandle;
import com.vke.core.vkz.types.imm.VkzImmediateFileChunk;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class VkzDirLayer implements Serializer<VkzDirLayer>, VkzDirectoryHandle {
    static VkzDirLayer SERIALIZER = new VkzDirLayer();

    private static final AtomicInteger LOCKID_GEN = new AtomicInteger();
    private int lockId = LOCKID_GEN.getAndDecrement();
    private VkzName layerName;
    private VkzArray<VkzEntry> entries;
    private VkzArray<VkzDirLayer> layers;

    private VkzFileHandle[] arrayRef;
    private VkzArchive archive;

    public void setFilesSource(VkzFileHandle[] arrayRef) {
        this.arrayRef = arrayRef;
        for (VkzDirLayer subLayer : layers.elements()) {
            subLayer.setFilesSource(arrayRef);
        }
    }

    public void setArchive(VkzArchive archive) {
        this.archive = archive;
        for (VkzDirLayer sub : layers.elements()) {
            sub.setArchive(archive);
        }
    }

    @Override
    public Class<?> getObjectClass() {
        return VkzDirLayer.class;
    }

    @Override
    public void save(VkzDirLayer value, Saver saver) throws SaveException {
        value.entries.save(saver);
        value.layers.save(saver);
        Serializer.saveObject(value.layerName, saver);
    }

    @Override
    public VkzDirLayer load(Loader loader) throws LoadException {
        VkzName name = Serializer.loadObject(VkzName.class, loader);
        VkzArray<VkzEntry> entries = new VkzArray<>(VkzEntry.class, new VkzEntry[0]);
        entries.load(loader);
        VkzArray<VkzDirLayer> layers = new VkzArray<>(VkzDirLayer.class, new VkzDirLayer[0]);
        layers.load(loader);

        VkzDirLayer layer = new VkzDirLayer();
        layer.layerName = name;
        layer.entries = entries;
        layer.layers = layers;
        return layer;
    }

    @Override
    public NotifyingIterable<VkzFileHandle> iterateFiles() {
        if (arrayRef == null) return null;

        return new VkzDirFilesIter();
    }

    @Override
    public VkzFileHandle file(CharSequence name) {
        if (arrayRef == null) return null;

        for (VkzEntry entry : entries.elements()) {
            int offset = entry.getChunkOffset();
            VkzFileHandle file = arrayRef[offset];
            if (file.getName().equals(name.toString())) {
                return file;
            }
        }
        return null;
    }

    @Override
    public VkzDirectoryHandle subDir(CharSequence name) {
        for (VkzDirLayer layer : layers.elements()) {
            if (layer.layerName.getName().equals(name.toString())) {
                return layer;
            }
        }
        return null;
    }

    private class VkzDirFilesIter implements NotifyingIterable<VkzFileHandle> {
        private int index;

        private VkzDirFilesIter() {
        }

        @Override
        public boolean hasNext() {
            return index < entries.length();
        }

        @Override
        public VkzFileHandle next() {
            if (archive != null) {
                archive.lock(lockId);
            }
            VkzEntry entry = entries.elements()[index++];
            return arrayRef[entry.getChunkOffset()];
        }

        @Override
        public void notifyEnd() {
            if (archive != null) {
                archive.unlock(lockId);
            }
        }
    }
}
