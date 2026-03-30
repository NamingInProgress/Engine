package com.vke.core.file.deflate.decompress.huffman;

import com.vke.core.file.deflate.decompress.BitUtils;

public record Code(int code, int codeLength, int symbol) {
    @Override
    public String toString() {
        return "Symbol: " + symbol + ", length: " + codeLength + ", code: " + BitUtils.intToBinStr(code);
    }
}
