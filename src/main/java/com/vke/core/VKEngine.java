package com.vke.core;

import com.vke.api.game.Game;
import com.vke.api.logger.Logger;
import com.vke.api.registry.VKERegistrate;
import com.vke.api.registry.VKERegistries;
import com.vke.core.logger.SOUT;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.rendering.vulkan.VulkanRenderer;
import com.vke.core.rendering.vulkan.shader.ShaderCompiler;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.window.Window;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.*;

public class VKEngine {
    private final Logger logger;
    private final Logger soutLogger;

    private final Window window;

    private final VulkanRenderer renderer;
    private final ShaderCompiler compiler;

    public static final VKERegistrate VKE_REGISTRATE = VKERegistries.get("vke");

    private final EngineCreateInfo createInfo;

    public VKEngine(EngineCreateInfo createInfo) {
        this.createInfo = createInfo;
        logger = LoggerFactory.get(VKEngine.class.getName());
        soutLogger = LoggerFactory.get(SOUT.TAG);
        SOUT.redirect(soutLogger);
        this.window = new Window(this, createInfo.windowCreateInfo);
        this.compiler = new ShaderCompiler();

        this.renderer = new VulkanRenderer(this, createInfo, createInfo.vulkanCreateInfo.framesInFlight);

        GLFW.glfwShowWindow(this.window.getHandle());
    }

    public void start(Game game) {
        game.onInit(this);

        while (!GLFW.glfwWindowShouldClose(window.getHandle())) {
            renderer.draw();

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
        this.compiler.free();
    }
    public Window getWindow() {
        return this.window;
    }
    public Logger getLogger() {
        return logger;
    }
    public ShaderCompiler getCompiler() { return this.compiler; }
    public boolean isDebugMode() { return !this.createInfo.releaseMode; }

}
