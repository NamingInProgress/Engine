package com.vke.core.vulkan;

import com.vke.core.rendering.vulkan.VulkanRenderer;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.rendering.vulkan.swapchain.SwapChain;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkViewport;

public class Viewport {
    public int x, y, w, h;
    public int minDepth, maxDepth;

    public Viewport() {
        this(0, 0, -1, -1);
    }

    public Viewport(int x, int y, int w, int h) {
        this(x, y, w, h, 0, 1);
    }

    public Viewport(int x, int y, int w, int h, int minDepth, int maxDepth) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
    }

    public Viewport use(VulkanRenderer.FrameData data) {
        MemoryStack stack = data.getStack();
        SwapChain swapChain = data.swapChain();
        CommandBuffers cmd = data.cmd();

        int width = w < 0 ? swapChain.getExtent().width() : w;
        int height = h < 0 ? swapChain.getExtent().height() : h;

        VkViewport.Buffer viewportBuffer = VkViewport.calloc(1, stack);
        viewportBuffer.get(0)
                .set(x, height, width, -height, minDepth, maxDepth);


        cmd.setViewport(0, viewportBuffer);
        this.w = width;
        this.h = height;
        return this;
    }

    public int width() {
        return w;
    }

    public int height() {
        return h;
    }
}
