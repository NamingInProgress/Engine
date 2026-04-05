package com.vke.core.vz.file;

import com.vke.core.vz.FileChunks;
import com.vke.core.vz.VzArchive;
import com.vke.utils.io.RandomAccessInputStream;

import java.io.InputStream;

public class VzFile {
    private final VzArchive archive;
    public final int fid;
    public final FileChunks fileChunks;

    public VzFile(VzArchive archive, int fid, FileChunks fileChunks) {
        this.archive = archive;
        this.fid = fid;
        this.fileChunks = fileChunks;
    }

    /**
     * Opens an InputStream on this Vz File. You can read the entire file by using this stream. Note
     * that closing it has no effect except making the read() method return -1. To actually close
     * the connection, please close the parent Vz archive itself.
     * @return the stream
     */
    public InputStream openReadingStream() {
        return new VzFileInputStream(this, new RandomAccessInputStream(archive.getRandAccessFile()));
    }

    public VzMutableFile mutate() {
        return new VzMutableFile(this);
    }

    public long size() {
        return fileChunks.fsize;
    }

    public VzArchive getArchive() {
        return archive;
    }
}
