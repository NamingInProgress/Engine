package com.vke.core.vkz.types.imm;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.api.vkz.VkzDirectoryHandle;
import com.vke.api.vkz.VkzFileHandle;
import com.vke.core.vkz.types.VkzArray;
import com.vke.core.vkz.types.VkzEntry;
import com.vke.core.vkz.types.VkzName;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

import java.util.Iterator;

public class VkzImmediateDirLayer implements Serializer<VkzImmediateDirLayer>, VkzDirectoryHandle {
    public static VkzImmediateDirLayer SERIALIZER = new VkzImmediateDirLayer();

    protected VkzName layerName;
    protected VkzArray<VkzEntry> entries;
    protected VkzArray<VkzImmediateDirLayer> layers;

    private VkzFileHandle[] arrayRef;
    private VkzImmediateArchive archive;

    static VkzImmediateDirLayer empty(String name) {
        VkzImmediateDirLayer layer = new VkzImmediateDirLayer();
        layer.layerName = new VkzName(name);
        layer.entries = new VkzArray<>(VkzEntry.class, new VkzEntry[0]);
        layer.layers = new VkzArray<>(VkzImmediateDirLayer.class, new VkzImmediateDirLayer[0]);

        return layer;
    }

    void setArchive(VkzImmediateArchive archive) {
        this.archive = archive;
        for (VkzImmediateDirLayer layer : layers.elements()) {
            layer.setArchive(archive);
        }
    }

    public void setFilesSource(VkzFileHandle[] arrayRef) {
        this.arrayRef = arrayRef;
        for (VkzImmediateDirLayer subLayer : layers.elements()) {
            subLayer.setFilesSource(arrayRef);
        }
    }

    @Override
    public Class<?> getObjectClass() {
        return VkzImmediateDirLayer.class;
    }

    @Override
    public void save(VkzImmediateDirLayer value, Saver saver) throws SaveException {
        Serializer.saveObject(value.layerName, saver);
        value.entries.save(saver);
        value.layers.save(saver);
    }

    @Override
    public VkzImmediateDirLayer load(Loader loader) throws LoadException {
        VkzName name = Serializer.loadObject(VkzName.class, loader);
        VkzArray<VkzEntry> entries = new VkzArray<>(VkzEntry.class, new VkzEntry[0]);
        entries.load(loader);
        VkzArray<VkzImmediateDirLayer> layers = new VkzArray<>(VkzImmediateDirLayer.class, new VkzImmediateDirLayer[0]);
        layers.load(loader);

        VkzImmediateDirLayer layer = new VkzImmediateDirLayer();
        layer.layerName = name;
        layer.entries = entries;
        layer.layers = layers;
        return layer;
    }

    @Override
    public String getName() {
        return layerName.getName();
    }

    @Override
    public Iterator<VkzFileHandle> iterateFiles() {
        if (arrayRef == null) return null;

        return new VkzDirFilesIter();
    }

    @Override
    public Iterator<VkzDirectoryHandle> iterateDirectories() {
        return new VkzDirDirsIter();
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
        for (VkzImmediateDirLayer layer : layers.elements()) {
            if (layer.layerName.getName().equals(name.toString())) {
                return layer;
            }
        }
        return null;
    }

    @Override
    public VkzFileHandle createFile(CharSequence name) {
        for (VkzEntry entry : entries.elements()) {
            VkzFileHandle file = arrayRef[entry.getChunkOffset()];
            if (file.getName().equals(name.toString())) {
                return file;
            }
        }

        if (archive == null) return null;

        VkzImmediateFileChunk newChunk = new VkzImmediateFileChunk(archive, 0, name.toString(), 0);
        int index = archive.insertNewFile(newChunk);
        newChunk.setOffset(index);
        entries.add(new VkzEntry(index));
        return newChunk;
    }

    @Override
    public VkzDirectoryHandle createDirectory(CharSequence name) {
        for (VkzDirectoryHandle layer : layers.elements()) {
            if (layer.getName().equals(name.toString())) {
                return layer;
            }
        }

        VkzImmediateDirLayer layer = VkzImmediateDirLayer.empty(name.toString());
        layer.setArchive(archive);
        layer.setFilesSource(arrayRef);

        layers.add(layer);
        return layer;
    }

    @Override
    public void deleteFile(CharSequence name) {
        throw new UnsupportedOperationException("Files cannot be deleted yet");
    }

    @Override
    public void deleteDirectory(CharSequence name) {
        throw new UnsupportedOperationException("Directories cannot be deleted yet");
    }

    private class VkzDirFilesIter implements Iterator<VkzFileHandle> {
        private int index;

        private VkzDirFilesIter() {
        }

        @Override
        public boolean hasNext() {
            return index < entries.length();
        }

        @Override
        public VkzFileHandle next() {
            VkzEntry entry = entries.elements()[index++];
            return arrayRef[entry.getChunkOffset()];
        }
    }

    private class VkzDirDirsIter implements Iterator<VkzDirectoryHandle> {
        private int index;

        private VkzDirDirsIter() {
        }

        @Override
        public boolean hasNext() {
            return index < layers.length();
        }

        @Override
        public VkzDirectoryHandle next() {
            VkzImmediateDirLayer entry = layers.elements()[index++];
            return entry;
        }
    }
}
