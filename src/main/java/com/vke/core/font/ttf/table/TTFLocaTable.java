package com.vke.core.font.ttf.table;

import com.vke.core.font.ttf.TTFFile;
import com.vke.core.font.ttf.TTFReader;

public class TTFLocaTable {

    public final int[] offsets;

    public TTFLocaTable(TTFReader reader, TTFFile.TableInfo table, TTFMaxpTable maxp, TTFHeadTable head) {
        if (table == null) throw new IllegalStateException("Missing loca table!");
        reader.position(table.offset);

        offsets = new int[maxp.numGlyphs + 1];

        if (head.indexToLocFormat == 0) {
            for (int i = 0; i <= maxp.numGlyphs; i++) {
                offsets[i] = reader.u16() * 2;
            }
        } else if (head.indexToLocFormat == 1) {
            for (int i = 0; i <= maxp.numGlyphs; i++) {
                offsets[i] = (int) reader.u32();
            }
        } else {
            throw new IllegalArgumentException("Unknown indexToLocFormat: " + head.indexToLocFormat);
        }
    }

}
