package com.vke.core.file.jpeg.scan;

public record FrameComponent (
        int id,
        int horizontalSamplingFactor,
        int verticalSamplingFactor,
        int quantizationTable
) { }
