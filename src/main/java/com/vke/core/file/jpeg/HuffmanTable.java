package com.vke.core.file.jpeg;

import com.vke.core.file.deflate.decompress.huffman.HMSymbolDecoder;
import com.vke.core.file.jpeg.jfif.TableClass;

public record HuffmanTable(int destination, TableClass tableClass, int[] lengthCount, int[] symbols, HMSymbolDecoder decoder) {
}
