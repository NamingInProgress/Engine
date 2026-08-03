package com.vke.core.font.ttf;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.vke.api.font.Font;
import com.vke.core.file.utils.Ascii4;
import com.vke.core.font.ttf.table.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class TTFFont implements Font {

    public final TTFReader reader;
    public final TTFHeader header;
    public final IntObjectHashMap<TableInfo> tables = new IntObjectHashMap<>();

    public final TTFHeadTable head;
    public final TTFMaxpTable maxp;
    public final TTFCmapTable cmap;
    public final TTFHheaTable hhea;
    public final TTFHmtxTable hmtx;
    public final TTFLocaTable loca;
    public final TTFGlyfTable glyf;

    private final Font.Metadata meta;

    public TTFFont(InputStream is) throws IOException {
        this.reader = new TTFReader(is);
        this.header = new TTFHeader(reader);

        parseTables();

        this.head = new TTFHeadTable(reader, table("head"));
        this.maxp = new TTFMaxpTable(reader, table("maxp"));
        this.cmap = new TTFCmapTable(reader, table("cmap"));
        this.hhea = new TTFHheaTable(reader, table("hhea"));
        this.hmtx = new TTFHmtxTable(reader, table("hmtx"), maxp, hhea);
        this.loca = new TTFLocaTable(reader, table("loca"), maxp, head);
        this.glyf = new TTFGlyfTable(reader, table("glyf"), maxp, loca, hmtx);

        this.meta = new Metadata(maxp.numGlyphs, head.unitsPerEm,
                new SpacingInfo(hhea.ascent, hhea.descent, hhea.lineGap, (hhea.ascent - hhea.descent + hhea.lineGap)),
                new CaretInfo(hhea.caretSlopeRise, hhea.caretSlopeRun, hhea.caretOffset)
        );
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

    @Override
    public Glyph getGlyph(int codePoint) {
        return glyf.glyphs[cmap.glyphMap[codePoint]];
    }

    @Override
    public List<Glyph> getStringGlyphs(String s) {
        return s.codePoints().mapToObj(this::getGlyph).toList();
    }

    @Override
    public int unitsPerEm() {
        return head.unitsPerEm;
    }

    @Override
    public Metadata meta() {
        return this.meta;
    }

    @Override
    public float kern(int beforeIndex, int currentIndex) {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TTFFont ttfFont = (TTFFont) o;
        return Objects.equals(tables, ttfFont.tables) && Objects.equals(meta, ttfFont.meta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tables, meta);
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
