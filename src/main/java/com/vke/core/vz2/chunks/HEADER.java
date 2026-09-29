package com.vke.core.vz2.chunks;

import com.vke.core.file.utils.DataUtils;
import com.vke.core.vz2.io.RAInputStream;
import com.vke.core.vz2.io.RAOutputStream;
import com.vke.core.vz2.io.VzLoader;
import com.vke.core.vz2.io.VzSaver;
import com.vke.core.vz2.utils.Ptr;

import java.io.IOException;

public class HEADER implements VzSaver {
    public static final int MAGIC = 0x565A4949;

    public final int magic;
    public int version;
    public final Ptr<FTAB> pFTAB;
    public final Ptr<MEMBLOCK> pMEMBLOCK;

    public HEADER() {
        this.magic = MAGIC;
        this.version = 0;
        this.pFTAB = new Ptr<>(0, new FTAB.Loader());
        this.pMEMBLOCK = new Ptr<>(0, new MEMBLOCK.Loader());
    }

    public HEADER(int magic, int version, long pFTAB, long pMEMBLOCK) throws IOException {
        if (magic != MAGIC) {
            throw new IOException("Illegal magic bytes found: " + Integer.toHexString(magic) + ", expected: " + Integer.toHexString(MAGIC));
        }
        this.magic = magic;

        if (version != 0) {
            throw new IOException("Illegal version found: " + version + ", only version 0 exists yet!");
        }
        //ts waring is actually so ass bro
        this.version = version;
        this.pFTAB = new Ptr<>(pFTAB, new FTAB.Loader());
        this.pMEMBLOCK = new Ptr<>(pMEMBLOCK, new MEMBLOCK.Loader());
    }

    @Override
    public void save(RAOutputStream stream) throws IOException {
        DataUtils.writeU32LittleEndian(stream, magic);
        DataUtils.writeU8(stream, version);
        pFTAB.save(stream);
        pMEMBLOCK.save(stream);
    }

    public static class Loader implements VzLoader<HEADER> {
        @Override
        public HEADER load(RAInputStream stream) throws IOException {
            int magic = DataUtils.readU32LittleEndian(stream);
            int version = DataUtils.readU8(stream);
            long pFTAB = DataUtils.readU64LittleEndian(stream);
            long pMEMBLOCK = DataUtils.readU64LittleEndian(stream);
            return new HEADER(magic, version, pFTAB, pMEMBLOCK);
        }
    }
}
