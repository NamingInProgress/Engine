package com.vke.core.file.gzip.deflate.lz77;

public class Lz77Consts {
    //see: https://datatracker.ietf.org/doc/html/rfc1951#section-3.2.5
    public static final int[] LENGTH_CODES_BASE = {
            3,    4,   5,   6,   7,   8,   9,  10,  11,  13,
            15,  17,  19,  23,  27,  31,  35,  43,  51,  59,
            67,  83,  99, 115, 131, 163, 195, 227, 258
    };

    public static final int[] LENGTH_CODES_BITS = {
            0, 0, 0, 0, 0, 0, 0, 0, 1, 1,
            1, 1, 2, 2, 2, 2, 3, 3, 3, 3,
            4, 4, 4, 4, 5, 5, 5, 5, 0
    };

    public static final int[] DIST_CODES_BASE = {
            //cant be asked to format this lmao
            1, 2, 3, 4, 5, 7, 9, 13, 17, 25,
            33, 49, 65, 97, 129, 193, 257, 385, 513, 769,
            1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577
    };

    public static final int[] DIST_CODES_BITS = {
            0, 0, 0, 0, 1, 1, 2, 2, 3, 3,
            4, 4, 5, 5, 6, 6, 7, 7, 8, 8,
            9, 9, 10, 10, 11, 11, 12, 12, 13, 13
    };
}
