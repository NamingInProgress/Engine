package com.vke.core.file.ogg.vorbis.setup;

public record Residue(
        int type,
        int begin,
        int end,
        int partitionSize,
        int classifications,
        int classbook,
        int[] cascades,
        int[][] books
) {
}
