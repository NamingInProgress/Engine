package com.vke.core.file.jpeg.jfif.markers;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.core.file.deflate.decompress.huffman.Code;
import com.vke.core.file.deflate.decompress.huffman.HMSymbolDecoder;
import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.jpeg.HuffmanTable;
import com.vke.core.file.jpeg.jfif.JfifDataMarker;
import com.vke.core.file.jpeg.jfif.TableClass;
import com.vke.core.file.utils.BitPackerUtils;
import com.vke.core.file.utils.DataUtils;
import com.vke.utils.io.PositionedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class DHT extends JfifDataMarker {
    public final HuffmanTable[] huffmanTables;

    public DHT(InputStream stream) throws IOException {
        super(stream);

        int remaining = this.size - 2;
        ArrayList<HuffmanTable> tables = new ArrayList<>();
        while (remaining > 0) {
            PositionedInputStream tableStream = new PositionedInputStream(stream);
            int[] TcTh = BitPackerUtils.unpackU4BE(DataUtils.readU8(tableStream), 2);
            TableClass tableClass = IntEnum.fromInt(TableClass.values(), TcTh[0]);
            int destination = TcTh[1];

            int symbolCount = 0;
            int[] lengthCount = new int[16];
            for (int i = 0; i < 16; i++) {
                lengthCount[i] = DataUtils.readU8(tableStream);
                symbolCount += lengthCount[i];
            }

            int[] symbols = new int[symbolCount];
            int[] codeLengths = new int[symbolCount];
            int symbolIdx = 0;
            for (int i = 0; i < 16; i++) {
                int len = i + 1;
                int amt = lengthCount[i];
                for (int l = 0; l < amt; l++) {
                    int symbol = DataUtils.readU8(tableStream);
                    symbols[symbolIdx] = symbol;
                    codeLengths[symbolIdx] = len;
                    symbolIdx++;
                }
            }

            Code[] codes = HMSymbolDecoder.createCodesFromLengthsAndSymbols(codeLengths, symbols, 16);
            HMSymbolDecoder decoder = new HMSymbolDecoder(codes, BitOrdering.LSB_FIRST);

            tables.add(new HuffmanTable(destination, tableClass, lengthCount, symbols, decoder));

            remaining -= (int) tableStream.getPosition();
        }

        this.huffmanTables = tables.toArray(new HuffmanTable[0]);
    }
}
