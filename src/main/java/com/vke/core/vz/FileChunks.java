package com.vke.core.vz;

import com.vke.utils.Utils;

import java.util.Arrays;
import java.util.Comparator;

public class FileChunks {
    public long FCHNK_offset;
    public int nFDATA;
    public long fsize;
    public FDATAOffset[] FDATA_offsets;
    private int cursor;

    public FileChunks(long FCHNK_offset, int nFDATA, long fsize) {
        this.FCHNK_offset = FCHNK_offset;
        this.nFDATA = nFDATA;
        this.FDATA_offsets = new FDATAOffset[nFDATA];
        this.fsize = fsize;
    }

    void pushFDATA(long offset, long ord, int udata) {
        if (Utils.verifyArrayIndex(cursor, FDATA_offsets.length)) {
            FDATA_offsets[cursor++] = new FDATAOffset(ord, offset, udata);
        }
    }

    public void sort() {
        Arrays.sort(FDATA_offsets, Comparator.comparingLong(FDATAOffset::ord));
    }

    public record FDATAOffset(long ord, long offset, int udata) {
    }
}
