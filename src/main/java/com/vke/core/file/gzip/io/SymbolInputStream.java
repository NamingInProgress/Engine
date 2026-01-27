package com.vke.core.file.gzip.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

public abstract class SymbolInputStream implements Closeable {
    /**
     * Tries to read the next symbol. -1 is returned if the stream has no more elements
     * @return the returned symbol
     * @throws IOException
     */
    public abstract int readNextSymbol() throws IOException;

    /**
     * Tried to read the next {@code amount} symbols. The resulting array does not have to be of length {@code amount}.
     * @return
     * @throws IOException
     */
    public int[] readSymbols(int amount) throws IOException {
        //educated guess for capacity lmao
        ArrayList<Integer> read = new ArrayList<>(amount / 2);
        for (int i = 0; i < amount; i++) {
            int symbol = readNextSymbol();
            if (symbol == -1) {
                break;
            }
            read.add(symbol);
        }
        return read.stream().mapToInt(x -> x).toArray();
    }

    /**
     * Reads all the remaining symbols. The length of returned array is the exact amount.
     * @return All remaining symbols in this stream.
     * @throws IOException
     */
    public int[] readAllSymbols() throws IOException {
        return readSymbols(Integer.MAX_VALUE);
    }
}
