package com.vke.core.font.ttf.table;

import com.vke.core.font.ttf.Glyph;
import com.vke.core.font.ttf.TTFFont;
import com.vke.core.font.ttf.TTFReader;
import org.jetbrains.annotations.ApiStatus;

public class TTFGlyfTable {

    private final TTFReader reader;
    private final TTFLocaTable loca;
    private final TTFHmtxTable hmtx;
    private final TTFFont.TableInfo table;

    public final Glyph[] glyphs;
    
    public TTFGlyfTable(TTFReader reader, TTFFont.TableInfo table, TTFMaxpTable maxp, TTFLocaTable loca,
                        TTFHmtxTable hmtx) {
        if (table == null) throw new IllegalStateException("Missing glyf table!");
        this.reader = reader;
        this.loca = loca;
        this.hmtx = hmtx;
        this.table = table;

        glyphs = new Glyph[maxp.numGlyphs];

        for (int i = 0; i < maxp.numGlyphs; i++) {
            int offset = loca.offsets[i];
            int length = loca.offsets[i + 1] - offset;

            if (length == 0) {
                glyphs[i] = Glyph.Empty(hmtx, i);
            } else {
                glyphs[i] = new Glyph(this, reader, table.offset + offset, hmtx, i);
            }
        }
    }

    @ApiStatus.Internal // This method is to be used only by the Glyph file to resolve compound glyphs
    public Glyph getOrRead(int glyphIndex) {
        if (glyphs[glyphIndex] == null) {
            glyphs[glyphIndex] = new Glyph(this, reader, table.offset + loca.offsets[glyphIndex], hmtx, glyphIndex);
        }

        return glyphs[glyphIndex];
    }

}
