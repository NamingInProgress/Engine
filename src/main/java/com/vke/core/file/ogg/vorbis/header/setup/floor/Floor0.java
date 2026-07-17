package com.vke.core.file.ogg.vorbis.header.setup.floor;

public record Floor0(
        int f0_order,
        int f0_rate,
        int f0_barkMapSize,
        int f0_amplitudeBits,
        int f0_amplitudeOffset,
        int f0_numBooks,
        int[] f0_bookList
) {}
