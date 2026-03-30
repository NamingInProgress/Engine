package com.vke.core.vkz.types;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.core.serializer.LoadException;
import com.vke.core.serializer.SaveException;

public class VkzEntry implements Serializer<VkzEntry> {
    public static VkzEntry SERIALIZER = new VkzEntry(0);

    private int chunkOffset;

    public VkzEntry(int chunkOffset) {
        this.chunkOffset = chunkOffset;
    }

    public int getChunkOffset() {
        return chunkOffset;
    }

    @Override
    public Class<?> getObjectClass() {
        return VkzEntry.class;
    }

    @Override
    public void save(VkzEntry value, Saver saver) throws SaveException {
        saver.saveShort((short) value.chunkOffset);
    }

    @Override
    public VkzEntry load(Loader loader) throws LoadException {
        int chunkOffset = loader.loadShort();

        VkzEntry entry = new VkzEntry(0);
        entry.chunkOffset = chunkOffset;
        return entry;
    }
}
