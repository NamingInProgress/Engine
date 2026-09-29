package com.vke.core.vz2.chunks;

import com.vke.core.file.utils.DataUtils;
import com.vke.core.vz2.io.RAInputStream;
import com.vke.core.vz2.io.RAOutputStream;
import com.vke.core.vz2.io.VzLoader;
import com.vke.core.vz2.io.VzSaver;
import com.vke.core.vz2.utils.Ptr;

import java.io.IOException;

public class MEMBLOCK implements VzSaver {
    public long pBlock;
    public long size;
    public Ptr<MEMBLOCK> pNext;

    public MEMBLOCK() {
        this(0, 0, 0);
    }

    public MEMBLOCK(long pBlock, long size, long pNext) {
        this.pBlock = pBlock;
        this.size = size;
        this.pNext = new Ptr<>(pNext, new Loader());
    }

    @Override
    public void save(RAOutputStream stream) throws IOException {
        DataUtils.writeU64LittleEndian(stream, pBlock);
        DataUtils.writeU64LittleEndian(stream, size);
        pNext.save(stream);
    }

    public static class Loader implements VzLoader<MEMBLOCK> {
        @Override
        public MEMBLOCK load(RAInputStream stream) throws IOException {
            long pBlock = DataUtils.readU64LittleEndian(stream);
            long size = DataUtils.readU64LittleEndian(stream);
            long pNext = DataUtils.readU64LittleEndian(stream);
            return new MEMBLOCK(pBlock, size, pNext);
        }
    }
}
