package com.vke.core.vz;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.vke.core.file.utils.DataUtils;
import com.vke.core.vz.chunks.HEADER;
import com.vke.utils.io.RandomAccessInputStream;

import java.io.IOException;

public class ChunkInfo {
    private IntObjectHashMap<FileChunks> fileChunks;
    private long thisOffset;
    private long skip;
    private long prev;

    private int remainingFiles;
    private int remainingChunks;

    private RandomAccessInputStream tmpStream;

    public ChunkInfo(RandomAccessInputStream stream, HEADER header) throws IOException {
        this.tmpStream = stream;
        this.fileChunks = new IntObjectHashMap<>();
        this.remainingFiles = header.nfiles;
        while(remainingFiles + remainingChunks > 0) {
            readChunk();
            tmpStream.seek(thisOffset + skip);
        }
        this.tmpStream = null;
        for (ObjectCursor<FileChunks> fc : fileChunks.values()) {
            fc.value.sort();
        }
    }

    private void readChunk() throws IOException {
        this.thisOffset = tmpStream.position();
        int ctype = DataUtils.readU8(tmpStream);
        if (ctype == 0) {
            //FCHNK
            //YES only read 32 bit here look at spec
            this.remainingFiles--;
            this.skip = DataUtils.readU32LittleEndian(tmpStream);
            this.prev = DataUtils.readU64LittleEndian(tmpStream);
            int fid = DataUtils.readU32LittleEndian(tmpStream);
            long fsize = DataUtils.readU64LittleEndian(tmpStream);
            int nchunks = DataUtils.readU16LittleEndian(tmpStream);
            this.remainingChunks += nchunks;
            FileChunks fc = new FileChunks(thisOffset, nchunks, fsize);
            this.fileChunks.put(fid, fc);
        } else if (ctype == 1) {
            //FDATA
            this.remainingChunks--;
            int fid = DataUtils.readU32LittleEndian(tmpStream);
            this.skip = DataUtils.readU64LittleEndian(tmpStream);
            this.prev = DataUtils.readU64LittleEndian(tmpStream);
            int flags = DataUtils.readU8(tmpStream);
            if ((flags & 1) != 0) {
                //this chunk is invalidated and cannot be used anymore!
            } else {
                long ord = DataUtils.readU64LittleEndian(tmpStream);
                long adler32 = DataUtils.readU32LittleEndian(tmpStream);
                long udata = DataUtils.readU32LittleEndian(tmpStream);
                FileChunks fc = this.fileChunks.get(fid);
                fc.pushFDATA(thisOffset, ord, udata);
            }
        } else {
            throw new IOException("Invalid chunk type: " + ctype);
        }
    }

    public FileChunks getFileChunks(int fid) {
        return fileChunks.get(fid);
    }
}
