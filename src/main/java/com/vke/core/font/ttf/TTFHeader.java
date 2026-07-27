package com.vke.core.font.ttf;

public class TTFHeader {

    public final long sfntVersion;
    public final int numTables;
    public final int searchRange;
    public final int entrySelector;
    public final int rangeShift;

    public TTFHeader(TTFReader reader) {
        this.sfntVersion = reader.u32();
        this.numTables = reader.u16();
        this.searchRange = reader.u16();
        this.entrySelector = reader.u16();
        this.rangeShift = reader.u16();
    }

}
