package com.vke.core.vz.chunks;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IPATH implements VzChunk{
    public long phash;
    public int npath;
    public int[] lpath;
    public int[][] pseg;
    public int fid;
    public int pnext;

    @Override
    public void write(OutputStream stream) throws IOException {
        DataUtils.writeU64LittleEndian(stream, phash);
        DataUtils.writeU8(stream, npath);

        for (int i = 0; i < npath; i++) {
            DataUtils.writeU8(stream, lpath[i]);
        }

        for (int i = 0; i < npath; i++) {
            for (int j = 0; j < lpath[i]; j++) {
                DataUtils.writeU8(stream, pseg[i][j]);
            }
        }

        DataUtils.writeU32LittleEndian(stream, fid);
        DataUtils.writeU32LittleEndian(stream, pnext);
    }

    public static class F implements Factory<IPATH> {

        @Override
        public IPATH read(InputStream stream) throws IOException {
            IPATH ipath = new IPATH();
            ipath.phash = DataUtils.readU64LittleEndian(stream);
            ipath.npath = DataUtils.readU8(stream);

            ipath.lpath = new int[ipath.npath];
            for (int i = 0; i < ipath.npath; i++) {
                ipath.lpath[i] = DataUtils.readU8(stream);
            }

            ipath.pseg = new int[ipath.npath][];
            for (int i = 0; i < ipath.npath; i++) {
                ipath.pseg[i] = new int[ipath.lpath[i]];
                for (int j = 0; j < ipath.lpath[i]; j++) {
                    ipath.pseg[i][j] = DataUtils.readU8(stream);
                }
            }

            ipath.fid = DataUtils.readU32LittleEndian(stream);
            ipath.pnext = DataUtils.readU32LittleEndian(stream);

            return ipath;
        }
    }
}
