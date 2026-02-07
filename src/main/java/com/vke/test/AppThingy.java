package com.vke.test;

import com.vke.api.app.App;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.Scissor;
import com.vke.core.rendering.vulkan.Viewport;
import com.vke.core.rendering.vulkan.VulkanRenderer;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.rendering.vulkan.pipeline.RenderPipelines;
import com.vke.core.window.Window;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;

public class AppThingy extends App {
    @Override
    public void onInit(VKEngine engine) {

    }

    @Override
    public void onDraw(Window window, VulkanRenderer.FrameData fd) {
        CommandBuffers cmd = fd.cmd();
        MemoryStack stack = fd.getStack();
        cmd.bindRenderPipeline(RenderPipelines.MAIN);
        cmd.setPushConstants(RenderPipelines.MAIN, stack);

        new Scissor().use(fd);
        new Viewport().use(fd);

        VK14.vkCmdDraw(cmd.getBuffer(), 3, 1, 0, 0);
    }

    @Override
    public void free() {

    }
}
