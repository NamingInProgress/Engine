package com.vke.core.vkz.types.lo;

import com.vke.api.vkz.ProgressReport;
import com.vke.api.vkz.VkzArchive;
import com.vke.api.vkz.VkzDirectoryHandle;
import com.vke.api.vkz.VkzFileHandle;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class VkzListOnlyArchive implements VkzArchive {
    @Override
    public VkzFileHandle file(CharSequence path) {
        return null;
    }

    @Override
    public VkzDirectoryHandle directory(CharSequence path) {
        return null;
    }

    @Override
    public VkzDirectoryHandle root() {
        return null;
    }

    @Override
    public Iterator<VkzFileHandle> iterateFiles() {
        return null;
    }

    @Override
    public void writeOut(OutputStream stream, ProgressReport.Listener progressListener) throws IOException {

    }
}
