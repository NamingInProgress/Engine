package com.vke.core.vz.chunks;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Pipe;

public class HEADER implements VzChunk {
    public int magic;
    public int version;
    public int vzdver;
    public long pindex;
    public int nfiles;

    @Override
    public void write(OutputStream stream) throws IOException {
        DataUtils.writeU32LittleEndian(stream, magic);
        DataUtils.writeU16LittleEndian(stream, version);
        DataUtils.writeU32LittleEndian(stream, vzdver);
        DataUtils.writeU64LittleEndian(stream, pindex);
        DataUtils.writeU32LittleEndian(stream, nfiles);
    }

    public static class F implements Factory<HEADER> {

        @Override
        public HEADER read(InputStream stream) throws IOException {
            HEADER header = new HEADER();
            header.magic = DataUtils.readU32LittleEndian(stream);
            header.version = DataUtils.readU16LittleEndian(stream);
            header.vzdver = DataUtils.readU32LittleEndian(stream);
            header.pindex = DataUtils.readU64LittleEndian(stream);
            header.nfiles = DataUtils.readU32LittleEndian(stream);
            return header;
        }
    }
}
