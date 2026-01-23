package com.vke.core.vkz.types.imm;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class VkzImmLockingInputStream extends ByteArrayInputStream {
    private final VkzImmediateFileChunk file;

    public VkzImmLockingInputStream(VkzImmediateFileChunk file, byte[] buf) {
        super(buf);
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        super.close();
        file.unlock();
    }
}
