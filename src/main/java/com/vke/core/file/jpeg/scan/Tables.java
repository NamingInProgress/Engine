package com.vke.core.file.jpeg.scan;

import com.vke.core.file.jpeg.ArithmeticTable;
import com.vke.core.file.jpeg.HuffmanTable;
import com.vke.core.file.jpeg.QuantTable;

public class Tables {
    private static final int N_SLOTS = 4;

    private final HuffmanTable[] huffmanTables;
    private final ArithmeticTable[] arithmeticTables;
    private final QuantTable[] quantTables;

    public Tables() {
        this.huffmanTables = new HuffmanTable[N_SLOTS];
        this.arithmeticTables = new ArithmeticTable[N_SLOTS];
        this.quantTables = new QuantTable[N_SLOTS];
    }

    public void installHuffmannTable(HuffmanTable table) {
        this.huffmanTables[table.destination()] = table;
    }

    public void installArithmeticTable(ArithmeticTable table) {
        this.arithmeticTables[table.destination()] = table;
    }

    public void installQuantTable(int slot, QuantTable table) {
        this.quantTables[slot] = table;
    }

    public HuffmanTable getHuffmanTable(int slot) {
        return this.huffmanTables[slot];
    }

    public ArithmeticTable getArithmeticTable(int slot) {
        return this.arithmeticTables[slot];
    }

    public QuantTable getQuantTable(int slot) {
        return this.quantTables[slot];
    }
}
