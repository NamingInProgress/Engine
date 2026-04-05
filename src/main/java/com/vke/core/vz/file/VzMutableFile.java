package com.vke.core.vz.file;

import com.vke.core.vz.ChunkRange;
import com.vke.core.vz.VzArchive;

public class VzMutableFile {
    private final VzFile file;

    VzMutableFile(VzFile file) {
        this.file = file;
    }

    /**
     * Appends b at the end of this files
     * @param b the bytes
     * @param off offset into b
     * @param len amount of bytes to append
     */
    public void append(byte[] b, int off, int len) {

    }

    /**
     * Clears all the data from this file
     */
    public void clear() {

    }

    /**
     * Truncates this file to a new size
     * @param len the size
     */
    public void truncate(long len) {

    }

    /**
     * Insert b into this file
     * @param b the bytes
     * @param off the offset into b
     * @param len the amount of bytes to insert
     * @param foff the offset into the file
     */
    public void insert(byte[] b, int off, int len, long foff) {

    }

    /**
     * Delete len amount of bytes at off from this file
     * @param off the offset into the file
     * @param len the amount of bytes to delete
     */
    public void delete(long off, long len) {
        VzArchive archive = file.getArchive();
        ChunkRange range = archive.getChunkRange(file.fid, off, len);

    }

    /**
     * Writes the specified bytes into this file
     * @param b the bytes
     * @param off the offset where to begin in bytes
     * @param len the amount of bytes to write
     * @param foff where in the file to write these bytes
     */
    public void write(byte[] b, int off, int len, long foff) {

    }
}
