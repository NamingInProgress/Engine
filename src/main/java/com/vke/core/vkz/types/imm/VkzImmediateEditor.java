package com.vke.core.vkz.types.imm;

import com.vke.api.vkz.VkzEditor;

import java.util.Arrays;

public class VkzImmediateEditor implements VkzEditor {
    private boolean clearFlag;
    private byte[] data;

    public VkzImmediateEditor() {
        this.data = new byte[0];
    }

    @Override
    public void clear() {
        if (clearFlag) {
            data = Arrays.copyOf(data, 0);
        }
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
        //todo: notify this archive that a file has been changed
        // also: maybe check if there is a current iteration
        // actually use the task scheduler to schedule a FileEdited task or smth and the archive waits for all
        // reading operations on either global file iter or on this directory to finish
        // then update the file lengths array.
    }
}
