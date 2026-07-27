package com.vke.core.font.ttf;

import com.vke.core.font.ttf.table.TTFCmapTable;
import com.vke.core.font.ttf.table.TTFHeadTable;
import com.vke.core.font.ttf.table.TTFLocaTable;
import com.vke.core.font.ttf.table.TTFMaxpTable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class TTFFile {

    public final TTFReader reader;
    public final TTFHeader header;
    public final HashMap<String, TableInfo> tables = new HashMap<>();

    public final TTFHeadTable head;
    public final TTFCmapTable cmap;
    public final TTFMaxpTable maxp;
    public final TTFLocaTable loca;

    public TTFFile(InputStream is) throws IOException {
        this.reader = new TTFReader(is);
        this.header = new TTFHeader(reader);

        parseTables();

        this.head = new TTFHeadTable(reader, tables.get("head"));
        this.cmap = new TTFCmapTable(reader, tables.get("cmap"));
        this.maxp = new TTFMaxpTable(reader, tables.get("maxp"));
        this.loca = new TTFLocaTable(reader, tables.get("loca"), maxp, head);
    }

    private void parseTables() {
        for (int i = 0; i < header.numTables; i++) {
            TableInfo info = new TableInfo(reader);
            tables.put(info.tag, info);
        }
    }

    public static class TableInfo {
        public final String tag; // TODO: replace tag with Ascii4 for faster map lookup
        public final long checksum;
        public final long offset;
        public final long length;

        public TableInfo(TTFReader reader) {
            this.tag = reader.tag();
            this.checksum = reader.u32();
            this.offset = reader.u32();
            this.length = reader.u32();
        }
    }

}
