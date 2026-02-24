package com.vke.core.file.deflate.compress.lz77;

public record Lz77Symbol(boolean isLiteral, byte literal, int length, int distance) {

}
