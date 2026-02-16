package com.vke.core.rendering.imageloading;

import java.nio.ByteBuffer;

public record ImageData(int width, int height, ByteBuffer pixels) { }
