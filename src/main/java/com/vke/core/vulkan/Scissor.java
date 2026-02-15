package com.vke.core.vulkan;

import com.vke.core.rendering.vulkan.VulkanRenderer;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.rendering.vulkan.swapchain.SwapChain;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;

public class Scissor {
    public int x, y, w, h;

    public Scissor() {
        this.w = -1;
        this.h = -1;
    }

    public Scissor(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void use(VulkanRenderer.FrameData data) {
        MemoryStack stack = data.getStack();
        SwapChain swapChain = data.swapChain();

        int width = w < 0 ? swapChain.getExtent().width() : w;
        int height = h < 0 ? swapChain.getExtent().height() : h;

        VkRect2D.Buffer scissorBuffer = VkRect2D.calloc(1, stack);
        scissorBuffer.get(0)
                .set(VkRect2D.calloc(stack)
                        .offset(VkOffset2D.calloc(stack).set(x, y))
                        .extent(VkExtent2D.calloc(stack).set(width, height))
                );

        CommandBuffers cmd = data.cmd();
        cmd.setScissor(0, scissorBuffer);
    }
}
