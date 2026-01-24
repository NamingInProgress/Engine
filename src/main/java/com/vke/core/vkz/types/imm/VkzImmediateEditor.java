package com.vke.core.vkz.types.imm;

import com.vke.api.vkz.VkzEditor;

import java.util.Arrays;

public class VkzImmediateEditor implements VkzEditor {
    private boolean clearFlag;
    private byte[] data;
    private final VkzImmediateFileChunk chunk;

    public VkzImmediateEditor(VkzImmediateFileChunk chunk) {
        this.data = new byte[0];
        this.chunk = chunk;
    }

    @Override
    public void clear() {
        data = Arrays.copyOf(data, 0);
        clearFlag = true;
    }

    @Override
    public void write(byte[] data, int start, int endInclusive) {
        int newDataAmt = endInclusive - start + 1;
        if (newDataAmt <= 0) return;
        int dataStart = this.data.length;
        this.data = Arrays.copyOf(this.data, newDataAmt);
        System.arraycopy(data, start, this.data, dataStart, newDataAmt);
    }

    @Override
    public void commit() {
        EditedPacket packet = new EditedPacket(clearFlag, data);
        chunk.runEdit(packet);
    }

    public record EditedPacket(boolean clearFlag, byte[] data) {
    }
}
