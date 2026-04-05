package com.vke.core.vz.index;

import com.vke.core.vz.VzUtils;
import com.vke.core.vz.chunks.INDEX;
import com.vke.core.vz.chunks.IPATH;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PathTable {
    private TableEntry[] table;
    private int MASK;

    public PathTable(InputStream inputStream) throws IOException {
        INDEX index = new INDEX.F().read(inputStream);
        this.table = new TableEntry[index.ntable];
        this.MASK = index.ntable - 1;

        IPATH[] rawEntries = new IPATH[index.ntable];
        for (int i = 0; i < index.ntable; i++) {
            rawEntries[i] = new IPATH.F().read(inputStream);
        }

        IPATH[] rawLinks = new IPATH[index.ntable];
        for (int i = 0; i < index.nlinks; i++) {
            rawLinks[i] = new IPATH.F().read(inputStream);
        }

        int i = 0;
        for (IPATH path : rawEntries) {
            TableEntry entry = new TableEntry(path, rawLinks);
            this.table[i++] = entry;
        }
    }

    public int resolveFID(String path) {
        byte[] bytes = path.getBytes(StandardCharsets.UTF_8);
        long hash = VzUtils.FNV1a(bytes, true);
        int index = (int) (hash & MASK);
        TableEntry entry = table[index];
        while (!entry.getPathString().equals(path)) {
            entry = entry.pnext;
            if (entry == null) return -1;
        }
        return entry.fid;
    }
}
