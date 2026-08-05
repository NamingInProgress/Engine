package com.vke.core.font.ttf.table;

import com.vke.core.font.ttf.TTFFont;
import com.vke.core.font.ttf.TTFReader;

public class TTFHheaTable {

    public final short ascent;
    public final short descent;
    public final short lineGap;

    public final short caretSlopeRise;
    public final short caretSlopeRun;
    public final short caretOffset;

    public final int numOfLongHorMetrics;

    public TTFHheaTable(TTFReader reader, TTFFont.TableInfo table) {
        if (table == null) throw new IllegalStateException("Missing hhea table!");
        reader.position(table.offset);
        reader.fixed(); // Skip version
        this.ascent = reader.fword();
        this.descent = reader.fword();
        this.lineGap = reader.fword();
        reader.skip(8);
        this.caretSlopeRise = reader.i16();
        this.caretSlopeRun = reader.i16();
        this.caretOffset = reader.fword();
        reader.skip(10);
        this.numOfLongHorMetrics = reader.u16();
    }

}
