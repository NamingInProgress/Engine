package com.vke.core.vulkan.descriptor.data;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.core.Context;
import com.vke.core.vulkan.buffers.MappedGpuRingBuffer;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.utils.Utils;
import com.vke.utils.io.Disposable;
import org.lwjgl.util.vma.Vma;

public class ShaderDataManager implements Disposable {

    public static final int MAX_OFFSETS_PER_FRAME = 1000;

    private static ShaderDataManager instance;

    private static boolean initialized = false;

    public static ShaderDataManager getInstance() {
        if (!initialized) throw new IllegalStateException("Called get instance before initializing!");
        if (instance == null) instance = new ShaderDataManager();
        return instance;
    }

    public static void initialize(ShaderDataCreateInfo createInfo) {
        initialized = true;
        ShaderDataManager self = getInstance();
        long alignedFrameBufSize = createInfo.minUboAlign == 1 ? createInfo.frameDataBufferSize : Utils.alignUpFast(createInfo.frameDataBufferSize, createInfo.minUboAlign);

        self.FRAME_DATA_BUFFER = new MappedGpuRingBuffer(createInfo.context.getEngine(), createInfo.device, alignedFrameBufSize * MAX_OFFSETS_PER_FRAME,
                createInfo.framesInFlight, BufferUsage.Bits.UBO.into());
        self.textures = new Texture[createInfo.maxTexturesCount];
    }

    public MappedGpuRingBuffer FRAME_DATA_BUFFER;

    public Texture[] textures;

    public void rotateBuffers() {
        FRAME_DATA_BUFFER.rotate();
    }

    public int texture(Texture tex) {
        int firstFree = -1;
        for (int i = 0; i < textures.length; i++) {
            if (textures[i] == tex) return i;
            if (textures[i] == null && firstFree == -1) {
                firstFree = i;
            }
        }
        if (firstFree == -1) throw new IllegalStateException("Out of texture slots!");

        textures[firstFree] = tex;
        return firstFree;
    }

    public void removeTexture(int index) {
        // TODO: implement
    }

    @Override
    public void free() {
        FRAME_DATA_BUFFER.free();
    }
}
