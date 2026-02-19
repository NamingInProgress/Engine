package com.vke.core.rendering.imageloading;

import com.vke.core.VKEngine;
import com.vke.core.file.png.PngFile;
import com.vke.core.memory.AutoHeapAllocator;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class LowLevelImageLoader {

    private final InputStream is;

    public LowLevelImageLoader(InputStream is) {
        this.is = is;
    }

    public ImageData decode(AutoHeapAllocator alloc) {
        //todo: replace with my png decoder once its done

        try {
            PngFile png = new PngFile(is);

            return new ImageData(
                    png.getPngInfo().width,
                    png.getPngInfo().height,
                    png.getOutput().argbToByteBuffer(alloc)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
