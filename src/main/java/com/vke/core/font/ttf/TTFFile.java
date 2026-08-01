package com.vke.core.font.ttf;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.vke.core.file.utils.Ascii4;
import com.vke.core.font.ttf.table.*;

import java.io.IOException;
import java.io.InputStream;

public class TTFFile {

    public final TTFReader reader;
    public final TTFHeader header;
    public final IntObjectHashMap<TableInfo> tables = new IntObjectHashMap<>();

    public final TTFHeadTable head;
    public final TTFCmapTable cmap;
    public final TTFMaxpTable maxp;
    public final TTFLocaTable loca;
    public final TTFGlyfTable glyf;

    public TTFFile(InputStream is) throws IOException {
        this.reader = new TTFReader(is);
        this.header = new TTFHeader(reader);

        parseTables();

        this.head = new TTFHeadTable(reader, table("head"));
        this.maxp = new TTFMaxpTable(reader, table("maxp"));
        this.cmap = new TTFCmapTable(reader, table("cmap"));
        this.loca = new TTFLocaTable(reader, table("loca"), maxp, head);
        this.glyf = new TTFGlyfTable(reader, table("glyf"), maxp, loca);
    }

    public TableInfo table(String s) {
        return table(Ascii4.of(s));
    }

    public TableInfo table(Ascii4 a) {
        return tables.get(a.toInt());
    }

    private void parseTables() {
        for (int i = 0; i < header.numTables; i++) {
            TableInfo info = new TableInfo(reader);
            tables.put(info.tag.toInt(), info);
        }
    }

    public static class TableInfo {
        public final Ascii4 tag;
        public final long checksum;
        public final long offset;
        public final long length;

        public TableInfo(TTFReader reader) {
            this.tag = Ascii4.of(reader.tag());
            this.checksum = reader.u32();
            this.offset = reader.u32();
            this.length = reader.u32();
        }
    }

}
