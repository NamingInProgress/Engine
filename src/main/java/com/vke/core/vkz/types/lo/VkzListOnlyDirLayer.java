package com.vke.core.vkz.types.lo;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Serializer;
import com.vke.api.vkz.VkzDirectoryHandle;
import com.vke.api.vkz.VkzFileHandle;
import com.vke.core.vkz.types.VkzArray;
import com.vke.core.vkz.types.VkzEntry;
import com.vke.core.vkz.types.VkzName;
import com.vke.utils.collection.IdxArrayIter;
import com.vke.core.serializer.LoadException;
import com.vke.utils.iter.Iter;

import java.util.Arrays;
import java.util.Iterator;

public class VkzListOnlyDirLayer implements VkzDirectoryHandle {
    private final VkzName name;
    private final VkzArray<VkzEntry> entries;
    //no vkz array cuz we use constructor here
    private final VkzListOnlyDirLayer[] subLayers;

    private VkzListOnlyArchive archive;

    public VkzListOnlyDirLayer(Loader loader) throws LoadException {
        name = Serializer.loadObject(VkzName.class, loader, false);

        entries = new VkzArray<>(VkzEntry.class, new VkzEntry[0]);
        entries.load(loader);

        subLayers = new VkzListOnlyDirLayer[loader.loadShort()];
        for (int i = 0; i < subLayers.length; i++) {
            subLayers[i] = new VkzListOnlyDirLayer(loader);
        }
    }

    void setArchive(VkzListOnlyArchive archive) {
        this.archive = archive;
    }

    @Override
    public String getName() {
        return name.getName();
    }

    @Override
    public Iter<VkzFileHandle> iterateFiles() {
        Iterator<Integer> indices = Arrays.stream(entries.elements())
                .map(VkzEntry::getChunkOffset)
                .iterator();
        return Iter.of(new IdxArrayIter<>(archive.files(), indices));
    }

    @Override
    public Iter<VkzDirectoryHandle> iterateDirectories() {
        return Iter.of(subLayers);
    }

    @Override
    public VkzFileHandle file(CharSequence name) {
        for (VkzEntry entry : entries.elements()) {
            VkzListOnlyFile file = archive.files()[entry.getChunkOffset()];
            if (file.getName().contentEquals(name)) {
                return file;
            }
        }
        return null;
    }

    @Override
    public VkzDirectoryHandle subDir(CharSequence name) {
        for (VkzListOnlyDirLayer layer : subLayers) {
            if (layer.getName().contentEquals(name)) {
                return layer;
            }
        }
        return null;
    }

    @Override
    public VkzFileHandle createFile(CharSequence name) {
        throw new UnsupportedOperationException("Cannot create new file in ListOnly vkz archive!");
    }

    @Override
    public VkzDirectoryHandle createDirectory(CharSequence name) {
        throw new UnsupportedOperationException("Cannot create new directory in ListOnly vkz archive!");
    }

    @Override
    public void deleteFile(CharSequence name) {
        throw new UnsupportedOperationException("Cannot delete file in ListOnly vkz archive!");
    }

    @Override
    public void deleteDirectory(CharSequence name) {
        throw new UnsupportedOperationException("Cannot delete directory in ListOnly vkz archive!");
    }
}
