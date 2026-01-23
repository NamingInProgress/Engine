package com.vke.core.vkz.types.imm;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.api.vkz.VkzEditor;
import com.vke.api.vkz.VkzFileHandle;
import com.vke.core.vkz.types.VkzName;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

public class VkzImmediateFileChunk implements VkzFileHandle {
    private final ReentrantLock lock = new ReentrantLock();

    private VkzName name;
    private byte[] data;
    private final int length;

    public VkzImmediateFileChunk(int length) {
        this.length = length;
        this.name = null;
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
        Serializer.saveObject(name, saver);
        for (byte b : data) saver.saveByte(b);
    }

    void load(Loader loader) throws LoadException {
        name = Serializer.loadObject(VkzName.class, loader);
        data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = loader.loadByte();
        }
    }

    void runEdit(VkzImmediateEditor.EditedPacket packet) {
        lock();
        try {
            if (packet.clearFlag()) {
                data = packet.data();
            } else {
                byte[] newData = packet.data();
                int oldLen = data.length;
                data = Arrays.copyOf(data, oldLen + newData.length);
                System.arraycopy(newData, 0, data, oldLen, newData.length);
            }
        } finally {
            unlock();
        }
    }
}
