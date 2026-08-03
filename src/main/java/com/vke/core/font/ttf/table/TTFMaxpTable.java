package com.vke.core.font.ttf.table;

import com.vke.core.font.ttf.TTFFont;
import com.vke.core.font.ttf.TTFReader;

public class TTFMaxpTable {

    public final int numGlyphs;

    public TTFMaxpTable(TTFReader reader, TTFFont.TableInfo table) {
        if (table == null) throw new IllegalStateException("Missing maxp table!");
        reader.position(table.offset);
        reader.fixed(); // skip version
        this.numGlyphs = reader.u16();
    }

}
