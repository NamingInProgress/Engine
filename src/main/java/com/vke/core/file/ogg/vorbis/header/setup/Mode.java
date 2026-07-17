package com.vke.core.file.ogg.vorbis.header.setup;

public record Mode (
        boolean blockFlag,
        int windowType,
        int transformType,
        int mapping
) {}
