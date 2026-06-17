package com.vke.core.file.ogg.vorbis.setup;

public record Mode (
        boolean blockFlag,
        int windowType,
        int transformType,
        int mapping
) {}
