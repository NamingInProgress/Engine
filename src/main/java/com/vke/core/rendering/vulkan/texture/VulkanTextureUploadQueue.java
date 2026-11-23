package com.vke.core.rendering.vulkan.texture;

import com.vke.api.event.Event;
import com.vke.api.event.EventBus;
import com.vke.api.event.EventListener;
import com.vke.api.event.SubscribeEvent;
import com.vke.api.rendering.abstraction.renderer.RenderDevice;
import com.vke.api.rendering.abstraction.renderer.commands.ImmediateSubmit;
import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
import com.vke.api.rendering.abstraction.renderer.enums.buffer.BufferUsage;
import com.vke.api.rendering.abstraction.renderer.enums.buffer.MemoryUsage;
import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageAspect;
import com.vke.api.rendering.vulkan.ImageLayout;
import com.vke.api.rendering.vulkan.ImageState;
import com.vke.api.rendering.vulkan.memory.VulkanImageBarrier;
import com.vke.core.framable.service.FramableManager;
import com.vke.core.image.Bitmap;
import com.vke.core.rendering.vulkan.VKEvents;
import com.vke.core.rendering.vulkan.buffers.StagedBuffer;
import com.vke.core.rendering.vulkan.buffers.premade.GeneralBuffer;
import com.vke.core.rendering.vulkan.command.VulkanCmdBuffers;
import com.vke.core.rendering.vulkan.device.VulkanRenderDevice;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.core.services2.Services;
import org.lwjgl.vulkan.VK14;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VulkanTextureUploadQueue implements EventListener {
    private final VulkanRenderSystem sys;
    private final List<QueuedUpload> queue = new ArrayList<>();
    private boolean separateTGQueues;
    private boolean loaded = false;

    public VulkanTextureUploadQueue(VulkanRenderSystem sys) {
        this.sys = sys;

        sys.<EventBus>service(Services.EVENT_BUS).register(this);
    }

    @SubscribeEvent
    public void onLoad(VKEvents.Loaded event) {
        loaded = true;
        this.separateTGQueues = !sys.device().getQueue(QueueType.GRAPHICS)
                .equals(sys.device().getQueue(QueueType.TRANSFER));
    }

    public synchronized void enqueue(VulkanTexture texture, Bitmap pixels) {
        this.queue.add(new QueuedUpload(texture, pixels));
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized void execute() {
        if (isEmpty()) return;
        if (!loaded) throw new RuntimeException("Tried to execute before loading finished.");

        if (separateTGQueues) {
            executeSeparate();
        } else {
            executeCombined();
        }
        queue.clear();
    }

    public void executeCombined() {
        VulkanRenderDevice device = sys.device();
        ArrayList<StagedBuffer> stagingBuffers = new ArrayList<>();

        try (ImmediateSubmit transfer = device.immediateSubmit()) {
            VulkanCmdBuffers tcmd = (VulkanCmdBuffers) transfer.cmd();

            for (QueuedUpload q : queue) {
                var bm = q.pxs;
                var t = q.texture;

                int size = bm.width() * bm.height() * bm.pixelStride();
                ByteBuffer buffer = device.allocate(size);
                bm.uploadIntoBytebuffer(buffer);
                buffer.flip();

                GeneralBuffer cpuBuf = new GeneralBuffer(buffer, true);
                StagedBuffer buf = new StagedBuffer(sys, cpuBuf,
                        new BufferUsage(BufferUsage.Bits.TRANSFER_SRC),
                        MemoryUsage.Bits.CPU_TO_GPU.into());
                stagingBuffers.add(buf);

                t.transition(tcmd, ImageState.TRANSFER_DST);

                buf.uploadViaStaging(transfer);
                tcmd.copyBufferToImage(buf.getGpuBuffer(), t, 0, 0);

                device.freeAllocation(buffer);

                t.transition(tcmd, ImageState.FRAGMENT_SHADER_READ);
            }
        }

        for (StagedBuffer stagingBuffer : stagingBuffers) {
            stagingBuffer.free();
        }
    }

    public void executeSeparate() {
        VulkanRenderDevice device = sys.device();
        ImmediateSubmit graphics = device.immediateSubmit(QueueType.GRAPHICS);
        ImmediateSubmit transfer = device.immediateSubmit(QueueType.TRANSFER, true, false);
        VulkanCmdBuffers tcmd = (VulkanCmdBuffers) transfer.cmd();
        VulkanCmdBuffers gcmd = (VulkanCmdBuffers) graphics.cmd();

        ArrayList<StagedBuffer> stagingBuffers = new ArrayList<>();

        graphics.waitSemaphore(transfer.signalSemaphore());

        graphics.manualFree(true);
        transfer.manualFree(true);

        for (QueuedUpload q : queue) {
            var bm = q.pxs;
            var t = q.texture;

            System.out.println("Uploading texture: " + t.getHandle());

            int size = bm.width() * bm.height() * bm.pixelStride();
            ByteBuffer buffer = device.allocate(size);
            bm.uploadIntoBytebuffer(buffer);
            buffer.flip();

            GeneralBuffer cpuBuf = new GeneralBuffer(buffer, true);
            StagedBuffer buf = new StagedBuffer(sys, cpuBuf,
                    new BufferUsage(BufferUsage.Bits.TRANSFER_SRC),
                    MemoryUsage.Bits.CPU_TO_GPU.into());
            stagingBuffers.add(buf);

            t.transition(tcmd, ImageState.TRANSFER_DST);

            buf.uploadViaStaging(transfer);
            tcmd.copyBufferToImage(buf.getGpuBuffer(), t, 0, 0);

            device.freeAllocation(buffer);

            t.transition(tcmd, new VulkanImageBarrier(
                    VK14.VK_PIPELINE_STAGE_2_COPY_BIT,
                    VK14.VK_PIPELINE_STAGE_2_NONE,
                    VK14.VK_ACCESS_2_TRANSFER_WRITE_BIT,
                    VK14.VK_ACCESS_2_NONE,
                    ImageLayout.TRANSFER_DST_OPTIMAL,
                    ImageLayout.SHADER_READONLY_OPTIMAL,
                    0, 1,
                    0, 1,
                    device.getTransferQueue().familyIndex(),
                    device.getGraphicsQueue().familyIndex(),
                    new ImageAspect(ImageAspect.Bits.COLOR)
            ), ImageState.FRAGMENT_SHADER_READ);
        }

        transfer.close();

        for (QueuedUpload q : queue) {
            var t = q.texture;

            t.transition(gcmd, new VulkanImageBarrier(
                    VK14.VK_PIPELINE_STAGE_2_NONE,
                    VK14.VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT,
                    VK14.VK_ACCESS_2_NONE,
                    VK14.VK_ACCESS_2_SHADER_READ_BIT,
                    ImageLayout.TRANSFER_DST_OPTIMAL,
                    ImageLayout.SHADER_READONLY_OPTIMAL,
                    0, 1,
                    0, 1,
                    device.getTransferQueue().familyIndex(),
                    device.getGraphicsQueue().familyIndex(),
                    new ImageAspect(ImageAspect.Bits.COLOR)
            ), ImageState.FRAGMENT_SHADER_READ);
        }

        graphics.close();

        for (StagedBuffer stagingBuffer : stagingBuffers) {
            stagingBuffer.free();
        }

        transfer.free();
        graphics.free();
    }

    public record QueuedUpload(VulkanTexture texture, Bitmap pxs) {

    }

}
