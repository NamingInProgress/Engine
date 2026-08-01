package com.vke.core.font.ttf.table;

import com.carrotsearch.hppc.IntArrayList;
import com.vke.core.font.ttf.TTFFile;
import com.vke.core.font.ttf.TTFReader;

public class TTFCmapTable {

    public final int numSubtables;
    public final Subtable[] subtables;
    public final Subtable used;

    public final int[] glyphMap;

    public TTFCmapTable(TTFReader reader, TTFFile.TableInfo tableInfo) {
        if (tableInfo == null) throw new IllegalStateException("Missing CMAP table!");
        reader.position(tableInfo.offset);
        reader.skip(2);
        numSubtables = reader.u16();

        this.subtables = new Subtable[numSubtables];
        for (int i = 0; i < numSubtables; i++) {
            subtables[i] = new Subtable(reader, tableInfo.offset);
        }

        Subtable best = null;
        int highestScore = 0;

        for (Subtable subtable : subtables) {
            int score = priority(subtable);
            if (score > highestScore) {
                best = subtable;
                highestScore = score;
            }
        }

        used = best;

        this.glyphMap = buildGlyphMap(reader);
    }

    private int[] buildGlyphMap(TTFReader reader) {
        reader.position(used.offset);
        if (used.format == 12) return buildGlyphMapF12(reader);
        if (used.format == 4) return buildGlyphMapF4(reader);
        return new int[0];
    }

    private int[] buildGlyphMapF12(TTFReader reader) {
        int[] glyphMap = new int[0x110000];

        reader.skip(4); // reserved

        long length = reader.u32();
        long language = reader.u32();
        long nGroups = reader.u32();

        for (long i = 0; i < nGroups; i++) {
            long startChar = reader.u32();
            long endChar = reader.u32();
            long startGlyph = reader.u32();

            for (long c = startChar; c <= endChar; c++) {
                glyphMap[(int) c] = (int) (startGlyph + (c - startChar));
            }
        }

        return glyphMap;
    }

    private int[] buildGlyphMapF4(TTFReader reader) {
        return new int[10];
    }

    private int priority(Subtable s) {
        if (s.platformID == 3 && s.platformSpecificID == 10 && s.format == 12) return 100;
        if (s.platformID == 0 && s.format == 12) return 90;
        if (s.platformID == 3 && s.platformSpecificID == 1 && s.format == 4) return 80;
        if (s.platformID == 0 && s.format == 4) return 70;
        return -1;
    }

    public static class Subtable {
        public final int platformID, platformSpecificID, format;
        public final long offset;

        public Subtable(TTFReader reader, long cmapOffset) {
            platformID = reader.u16();
            platformSpecificID = reader.u16();
            offset = cmapOffset + reader.u32();

            int pos = reader.position();
            reader.position(offset);
            format = reader.u16();
            reader.position(pos);
        }
    }

}
