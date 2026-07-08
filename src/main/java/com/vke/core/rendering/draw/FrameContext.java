package com.vke.core.rendering.draw;

import com.vke.api.rendering.abstraction.commands.CommandBuffer;
import com.vke.core.vulkan.extent.Extent2D;
import com.vke.core.vulkan.extent.VulkanExtentUtils;
import com.vke.core.window.Window;
import org.lwjgl.vulkan.VkExtent2D;

public class FrameContext {

    private final CommandBuffer cmd;
    private final Window window;
    private final Extent2D extent;

    public FrameContext(CommandBuffer cmd, VkExtent2D extent, Window window) {
        this.cmd = cmd;
        this.window = window;
        this.extent = VulkanExtentUtils.ofVk(extent);
    }

    public CommandBuffer getCommandBuffer() {
        return cmd;
    }
    public Window getWindow() {
        return window;
    }
    public Extent2D getExtent() { return extent; }
}
