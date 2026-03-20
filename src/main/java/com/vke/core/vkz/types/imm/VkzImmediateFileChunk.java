package com.vke.core.vkz.types.imm;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.api.vkz.VkzEditor;
import com.vke.api.vkz.VkzFileHandle;
import com.vke.core.vkz.types.VkzName;
import com.vke.core.serializer.LoadException;
import com.vke.core.serializer.SaveException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class VkzImmediateFileChunk implements VkzFileHandle {
    private final ReentrantLock lock = new ReentrantLock();

    private VkzName name;
    private byte[] data;
    private int length;
    private int offset;

    private final VkzImmediateArchive archive;

    public VkzImmediateFileChunk(VkzImmediateArchive archive, int length, int offset) {
        this.archive = archive;
        this.length = length;
        this.name = null;
        this.offset = offset;
    }

    VkzImmediateFileChunk(VkzImmediateArchive archive, int length, String name, int offset) {
        this.archive = archive;
        this.length = length;
        this.name = new VkzName(name);
        this.data = new byte[0];
        this.offset = offset;
    }

    @Override
    public InputStream getInputStream() {
        lock.lock();
        return new VkzImmLockingInputStream(this, data);
    }

    @Override
    public String getName() {
        return name != null ? name.getName() : null;
    }

    @Override
    public int getSize() {
        return length;
    }

    @Override
    public VkzEditor edit() {
        return new VkzImmediateEditor(this);
    }

    @Override
    public boolean isLocked() {
        return lock.isLocked();
    }

    void lock() {
        lock.lock();
    }

    void unlock() {
        lock.unlock();
    }

    void save(Saver saver) throws SaveException {
        Serializer.saveObject(name, saver, false);
        saver.saveRaw(data);
    }

    void load(Loader loader) throws LoadException {
        name = Serializer.loadObject(VkzName.class, loader, false);
        data = loader.loadRaw(length);
    }

    void runEdit(VkzImmediateEditor.EditedPacket packet) {
        lock();
        try {
            if (packet.clearFlag()) {
                data = packet.data();
            } else {
                byte[] newData = packet.data();
                int oldLen = data.length;
                int newLen = oldLen + newData.length;
                data = Arrays.copyOf(data, newLen);
                System.arraycopy(newData, 0, data, oldLen, newData.length);
            }
            length = data.length;
            archive.updateLength(offset, length);
        } finally {
            unlock();
        }
    }

    void setOffset(int offset) {
        this.offset = offset;
    }
}
