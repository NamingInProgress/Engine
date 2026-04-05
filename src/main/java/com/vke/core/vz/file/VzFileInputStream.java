package com.vke.core.vz.file;

import com.vke.core.file.deflate.decompress.InflatingDevice;
import com.vke.core.file.deflate.decompress.check.Adler;
import com.vke.core.file.utils.DataUtils;
import com.vke.core.vz.FileChunks;
import com.vke.utils.Utils;
import com.vke.utils.io.RandomAccessInputStream;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class VzFileInputStream extends InputStream {
    private final RandomAccessInputStream inputStream;
    private final VzFile file;
    private int currentChunkIndex;
    private InflatingDevice currentInflater;
    private Adler currentAdler;
    private int currentExpectedAdler;
    private boolean done;

    public VzFileInputStream(VzFile file, RandomAccessInputStream inputStream) {
        this.inputStream = inputStream;
        this.file = file;
        this.currentChunkIndex = 0;
    }

    private void findNextChunk() throws IOException {
        if (currentChunkIndex >= file.fileChunks.nFDATA) {
            currentInflater = null;
            done = true;
            return;
        }

        FileChunks.FDATAOffset offset = file.fileChunks.FDATA_offsets[currentChunkIndex];
        inputStream.seek(offset.offset());
        int ctype = DataUtils.readU8(inputStream);
        int fid = DataUtils.readU32LittleEndian(inputStream);
        long nskip = DataUtils.readU64LittleEndian(inputStream);
        long nprev = DataUtils.readU64LittleEndian(inputStream);
        int flags = DataUtils.readU8(inputStream);
        long ord = DataUtils.readU64LittleEndian(inputStream);
        this.currentExpectedAdler = DataUtils.readU32LittleEndian(inputStream);
        int ldata = DataUtils.readU32LittleEndian(inputStream);

        byte[] vdata = inputStream.readNBytes(ldata);
        if (ldata != vdata.length) {
            throw new EOFException();
        }
        this.currentAdler = new Adler();
        //TODO
        //for now load the entire chunk into memory. this might change in the future
        this.currentInflater = new InflatingDevice(currentAdler, new ByteArrayInputStream(vdata));

        currentChunkIndex += 1;
    }

    @Override
    public int read() throws IOException {
        if (currentInflater == null) {
            findNextChunk();
        }

        if (done) return -1;
        if (currentInflater.isFinished()) {
            currentInflater = null;
            int computedAdler = this.currentAdler.get();
            if (computedAdler != currentExpectedAdler) {
                throw new IOException("This file is corrupt! (Adler32 mismatch)");
            }
            return read();
        }
        return Utils.chainExceptions(currentInflater::inflateNextByte);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.currentInflater = null;
        this.done = true;
    }
}
