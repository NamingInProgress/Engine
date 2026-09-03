package com.vke.core.file.jpeg.scan;

public record Frame(
        FrameKind kind,
        int precision,
        int width,
        int height,
        FrameComponent[] components
) {}
