package com.vke.core.font.ttf.table;

import com.vke.core.font.ttf.TTFFont;
import com.vke.core.font.ttf.TTFReader;

public class TTFHeadTable {

    private static final long TTF_HEAD_MAGIC = 0x5F0F3CF5;

    public final double version;
    public final long magic;
    public final int flags;
    public final int unitsPerEm;
    public final long created, modified;
    public final short xMin, yMin, xMax, yMax;
    public final int macStyle;
    public final int lowestRecPPEM;
    public final short fontDirectionHint;
    public final short indexToLocFormat;
    public final short glyphDataFormat;

    public TTFHeadTable(TTFReader reader, TTFFont.TableInfo tableInfo) {
        if (tableInfo == null) throw new IllegalStateException("Missing HEAD table!");
        reader.position(tableInfo.offset);
        this.version = reader.fixed();
        reader.skip(8); // Skip font revision and check sum adjustment
        this.magic = reader.u32();
        this.flags = reader.u16();
        this.unitsPerEm = reader.u16();
        this.created = reader.u64();
        this.modified = reader.u64();
        this.xMin = reader.fword();
        this.yMin = reader.fword();
        this.xMax = reader.fword();
        this.yMax = reader.fword();
        this.macStyle = reader.u16();
        this.lowestRecPPEM = reader.u16();
        this.fontDirectionHint = reader.i16();
        this.indexToLocFormat = reader.i16();
        this.glyphDataFormat = reader.i16();

        if (this.magic != TTF_HEAD_MAGIC) throw new IllegalStateException("Head magic does not match!");
    }

}
