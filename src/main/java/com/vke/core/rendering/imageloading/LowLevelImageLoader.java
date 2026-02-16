package com.vke.core.rendering.imageloading;

import com.vke.core.VKEngine;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class LowLevelImageLoader {

    private final ByteBuffer rawData;

    public LowLevelImageLoader(ByteBuffer data) {
        this.rawData = data;
    }

    public ImageData decode(VKEngine engine) {
        //todo: replace with my png decoder once its done

        try (MemoryStack stack = MemoryStack.stackPush()) {

            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer pixels = STBImage.stbi_load_from_memory(
                    rawData,
                    width,
                    height,
                    channels,
                    4 // force RGBA //probably not chatGPT but for testing purposes sure
            );

            if (pixels == null) {
                engine.throwException(new ImageDecodeException("Failed to load image: " + STBImage.stbi_failure_reason()), "Low Level Image Loader");
            }

            return new ImageData(
                    width.get(0),
                    height.get(0),
                    pixels
            );
        }
    }
}
