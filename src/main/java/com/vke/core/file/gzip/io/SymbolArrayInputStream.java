package com.vke.core.file.gzip.io;

import java.io.IOException;

public class SymbolArrayInputStream extends SymbolInputStream {
    private final int[] symbols;
    private int cursor;

    public SymbolArrayInputStream(int[] symbols) {
        this.symbols = symbols;
    }

    @Override
    public int readNextSymbol() throws IOException {
        if (cursor >= symbols.length) {
            return -1;
        }
        return symbols[cursor++];
    }

    @Override
    public int[] readAllSymbols() throws IOException {
        int remaining = symbols.length - cursor;
        int[] out = new int[remaining];
        System.arraycopy(symbols, cursor, out, 0, remaining);
        return out;
    }

    @Override
    public void close() throws IOException {

    }
}
