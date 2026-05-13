package com.vke.test.ui;

import com.vke.api.scene.Scene;
import com.vke.api.window.WindowCreateInfo;
import com.vke.config.ConfigurationOption;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.scene.SceneApp;
import com.vke.core.services2.Services;
import com.vke.core.ui.geom.Fect;
import com.vke.core.ui.rendering.core.GeneralDrawRequest;
import com.vke.core.ui.rendering.core.UiGeneralVertex;
import com.vke.core.ui.rendering.core.passes.GeneralRenderPass;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.utils.io.Identifier;
import org.joml.Matrix4f;

public class GeneralPassTest extends Scene {
    private GeneralRenderPass renderPass;

    public GeneralPassTest(Identifier name, Context context) {
        super(name, context);
    }

    public static void main(String[] args) throws InterruptedException {
        EngineCreateInfo createInfo = new EngineCreateInfo("CUBE", "vke");
        createInfo.releaseMode = false;
        createInfo.vulkanCreateInfo.framesInFlight = 1;
        //createInfo.vsync = true;
        createInfo.windowCreateInfo = new WindowCreateInfo("UI GP test");

        ConfigurationOption<Boolean> renderdoc = new ConfigurationOption<>("renderdoc", ConfigurationOption.Initializer.BOOLEAN);

        VKEngine engine = new VKEngine(createInfo);

        if (renderdoc.get()) Thread.sleep(5000);

        engine.start(new SceneApp("uigeneralpass"));
    }

    @Override
    public void onLoad() {
        VKEngine engine = context.getEngine();
        VulkanRenderer renderer = context.service(Services.VULKAN_RENDERER).assumeImplementation();
        renderPass = new GeneralRenderPass(engine, renderer);
        renderPass.onResize(engine.getWindow().getSize());
    }

    @Override
    public void onDraw(DrawContext ctx) {
        UiGeneralVertex[] vertices = {
                new UiGeneralVertex(100, 100, 1, 1, 0, 0, 1, 0, 0, null, -1, -1),
                new UiGeneralVertex(200, 100, 1, 1, 0, 0, 1, 0, 0, null, -1, -1),
                new UiGeneralVertex(200, 200, 1, 1, 0, 0, 1, 0, 0, null, -1, -1),
                new UiGeneralVertex(100, 200, 1, 1, 0, 0, 1, 0, 0, null, -1, -1)
        };

        renderPass.acceptRequest(new GeneralDrawRequest(
                -1, -1, -1,
                vertices,
                new int[] {0, 1, 2, 2, 3, 0}
        ));

        renderPass.beginFrame(new Matrix4f[0], new Fect[0]);
        renderPass.draw(ctx);
    }

    @Override
    public void onUnload() {

    }

    @Override
    public void free() {

    }
}
