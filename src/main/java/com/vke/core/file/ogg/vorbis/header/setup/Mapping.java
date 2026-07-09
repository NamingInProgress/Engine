package com.vke.core.file.ogg.vorbis.header.setup;

public record Mapping(
        int submasks,
        int couplingSteps,
        int[] magnitude,
        int[] angle,
        int[] mux,
        int[] floor,
        int[] residue
) {}
