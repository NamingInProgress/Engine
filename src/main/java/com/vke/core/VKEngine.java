package com.vke.core;

import com.vke.api.game.Game;
import com.vke.api.logger.Logger;
import com.vke.api.registry.VKERegistrate;
import com.vke.api.registry.VKERegistries;
import com.vke.core.logger.SOUT;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.window.Window;
import org.lwjgl.glfw.GLFW;

public class VKEngine {
    private final Logger logger;
    private final Logger soutLogger;

    private final Window window;

    public static final VKERegistrate VKE_REGISTRATE = VKERegistries.get("vke");

    public VKEngine(EngineCreateInfo createInfo) {
        logger = LoggerFactory.get(VKEngine.class.getName());
        soutLogger = LoggerFactory.get(SOUT.TAG);
        SOUT.redirect(soutLogger);

        this.window = new Window(this, createInfo.windowCreateInfo);

        VulkanSetup vkSetup = new VulkanSetup(createInfo);
        vkSetup.initVulkan(this);
    }

    public void start(Game game) {
        game.onInit(this);

        while (!GLFW.glfwWindowShouldClose(window.getHandle())) {
            game.onDraw(window);

            GLFW.glfwPollEvents();
        }

        free();
    }

    public void throwException(Throwable e, String where) {
        logger.fatal("Fatal exception at %s", where);
        throw new RuntimeException(e);
    }

    private void free() {
        window.close();
    }

    public Window getWindow() {
        return this.window;
    }

    public Logger getLogger() {
        return logger;
    }
}
