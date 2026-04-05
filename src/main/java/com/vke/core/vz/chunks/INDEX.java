package com.vke.core.vz.chunks;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class INDEX implements VzChunk {
    public int ntable;
    public int nlinks;


    @Override
    public void write(OutputStream stream) throws IOException {
        DataUtils.writeU32LittleEndian(stream, ntable);
        DataUtils.writeU32LittleEndian(stream, nlinks);
    }

    public static class F implements Factory<INDEX> {

        @Override
        public INDEX read(InputStream stream) throws IOException {
            INDEX index = new INDEX();
            index.ntable = DataUtils.readU32LittleEndian(stream);
            index.nlinks = DataUtils.readU32LittleEndian(stream);
            return index;
        }
    }
}
