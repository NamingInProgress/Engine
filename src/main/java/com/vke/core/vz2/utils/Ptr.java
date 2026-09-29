package com.vke.core.vz2.utils;

import com.vke.core.file.utils.DataUtils;
import com.vke.core.vz2.io.RAInputStream;
import com.vke.core.vz2.io.RAOutputStream;
import com.vke.core.vz2.io.VzLoader;
import com.vke.core.vz2.io.VzSaver;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Ptr<P> implements VzSaver {
    private long pointer;
    private @Nullable P pointee;
    private final VzLoader<P> loader;

    public Ptr(long pointer, VzLoader<P> loader) {
        this.pointer = pointer;
        this.loader = loader;
    }

    public P pointee(RAInputStream stream) throws IOException {
        if (pointee == null) {
            stream.seek(pointer);
            pointee = loader.load(stream);
        }
        return pointee;
    }

    public void setPointer(long pointer) {
        this.pointer = pointer;
        this.pointee = null;
    }

    @Override
    public void save(RAOutputStream stream) throws IOException {
        DataUtils.writeU64LittleEndian(stream, pointer);
    }
}
