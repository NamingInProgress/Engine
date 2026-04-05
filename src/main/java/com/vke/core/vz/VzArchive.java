package com.vke.core.vz;

import com.vke.core.vz.chunks.HEADER;
import com.vke.core.vz.file.VzFile;
import com.vke.core.vz.index.PathTable;
import com.vke.utils.io.RandomAccessInputStream;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class VzArchive implements Closeable {
    private static final long FCHNK_START_OFFSET = 0x16;

    private HEADER header;
    private ChunkInfo chunkInfo;
    private PathTable pathTable;
    private RandomAccessFile randAccessFile;

    public VzArchive(File file) throws IOException {
        this.randAccessFile = new RandomAccessFile(file, "r");
        RandomAccessInputStream stream = new RandomAccessInputStream(randAccessFile);
        this.header = new HEADER.F().read(stream);

        this.chunkInfo = new ChunkInfo(stream, header);

        long indexPtr = header.pindex;
        stream.seek(indexPtr);
        this.pathTable = new PathTable(stream);
    }

    public VzFile resolveFile(String path) {
        int fid = pathTable.resolveFID(path);
        if (fid == -1) return null;
        return new VzFile(this, fid, chunkInfo.getFileChunks(fid));
    }

    public RandomAccessFile getRandAccessFile() {
        return randAccessFile;
    }

    public ChunkRange getChunkRange(int fid, long off, long len) {
        FileChunks fc = this.chunkInfo.getFileChunks(fid);
        if (fc == null) return null;
        long cursor = 0;
        int cindex = 0;
        while (cursor < off + len) {
            FileChunks.FDATAOffset fdata = fc.FDATA_offsets[cindex++];
            cursor += fdata.udata();
            if (cursor >= off) {

            }
        }
    }

    @Override
    public void close() throws IOException {
        randAccessFile.close();
    }
}
