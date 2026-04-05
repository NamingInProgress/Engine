package com.vke.core.vz.index;

import com.vke.core.vz.chunks.IPATH;
import com.vke.utils.io.SegmentedPath;

import java.nio.charset.StandardCharsets;

public class TableEntry {
    private long hash;
    private SegmentedPath path;
    private String stringFormat;
    public TableEntry pnext;
    public int fid;

    public TableEntry(IPATH ipath, IPATH[] links) {
        this.hash = ipath.phash;
        this.fid = ipath.fid;

        String[] parts = new String[ipath.npath];
        for (int compIdx = 0; compIdx < ipath.npath; compIdx++) {
            int componentLen = ipath.lpath[compIdx];
            byte[] bytes = new byte[componentLen];
            for (int i = 0; i < componentLen; i++) {
                bytes[i] = (byte) ipath.pseg[compIdx][i];
            }
            String part = new String(bytes, StandardCharsets.UTF_8);
            parts[compIdx] = part;
        }
        this.path = new SegmentedPath(parts, "/");
        this.stringFormat = this.path.toString();

        if (ipath.pnext != 0) {
            IPATH link = links[ipath.pnext - 1];
            this.pnext = new TableEntry(link, links);
        }
    }

    public SegmentedPath getPath() {
        return path;
    }

    public String getPathString() {
        return stringFormat;
    }
}
