package com.vke.core.font.ttf.table;

import com.vke.api.font.FontFace;
import com.vke.core.font.ttf.TTFFont;
import com.vke.core.font.ttf.TTFReader;

public class TTFOS2Table {

    public final int fsSelectionMerged;

    public TTFOS2Table(TTFReader reader, TTFFont.TableInfo tableInfo) {
        if (tableInfo == null) throw new IllegalStateException("Missing OS/2 Table!");
        reader.position(tableInfo.offset);
        reader.skip(62);
        int fsSelection = reader.u16();

        int merged = 0;
        int[] styles = FontFace.orderedStyles();

        for (int i = 0; i < 6; i++) {
            if ((fsSelection & (1 << i)) != 0) {
                merged |= styles[i];
            }
        }

        this.fsSelectionMerged = merged;
    }

}
