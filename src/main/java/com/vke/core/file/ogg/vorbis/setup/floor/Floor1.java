package com.vke.core.file.ogg.vorbis.setup.floor;

public record Floor1(
        int partitions,
        int[] partitionClassList,
        int[] classDimensions,
        int[] classSubclasses,
        int[] classMasterbooks,
        int[][] subclassBooks,
        int multiplier,
        int rangeBits,
        int[] xList
) {}
