package com.vke.core.rendering.vulkan.texture;

import com.vke.api.rendering.abstraction.renderer.commands.ImmediateSubmit;
import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
import com.vke.core.file.png.Pixels;
import com.vke.core.rendering.vulkan.device.VulkanRenderDevice;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;

import java.util.ArrayList;
import java.util.List;

public class VulkanTextureUploadQueue {

    private final VulkanRenderSystem sys;
    private final List<QueuedUpload> queue = new ArrayList<>();

    public VulkanTextureUploadQueue(VulkanRenderSystem sys) {
        this.sys = sys;
    }

    public void enqueue(VulkanTexture texture, Pixels pixels) {
        this.queue.add(new QueuedUpload(texture, pixels));
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void execute() {
        VulkanRenderDevice device = sys.device();
        ImmediateSubmit graphics = device.immediateSubmit(QueueType.GRAPHICS);
        ImmediateSubmit transfer = device.immediateSubmit(QueueType.TRANSFER, true);

        graphics.waitSemaphore(transfer.signalSemaphore());



        transfer.close();
        graphics.close();
    }

    public record QueuedUpload(VulkanTexture texture, Pixels pxs) {

    }

}
