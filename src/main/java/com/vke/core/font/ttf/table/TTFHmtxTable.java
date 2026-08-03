package com.vke.core.font.ttf.table;

import com.vke.core.font.ttf.TTFFont;
import com.vke.core.font.ttf.TTFReader;

public class TTFHmtxTable {

    public final LongHorMetric[] metrics;

    public TTFHmtxTable(TTFReader reader, TTFFont.TableInfo table, TTFMaxpTable maxp, TTFHheaTable hhea) {
        if (table == null) throw new IllegalStateException("Missing hmtx table!");
        reader.position(table.offset);
        this.metrics = new LongHorMetric[maxp.numGlyphs];

        for (int i = 0; i < hhea.numOfLongHorMetrics; i++) {
            metrics[i] = new LongHorMetric(reader.u16(), reader.i16());
        }

        int lastAdvanceWidth = metrics[hhea.numOfLongHorMetrics - 1].advanceWidth;
        for (int i = hhea.numOfLongHorMetrics; i < maxp.numGlyphs; i++) {
            metrics[i] = new LongHorMetric(lastAdvanceWidth, reader.i16());
        }
    }

    public record LongHorMetric(int advanceWidth, short leftSideBearing) {}

}
