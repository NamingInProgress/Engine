package com.vke;

import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.window.WindowCreateInfo;
import com.vke.config.ConfigurationOption;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.logger.CoreLogger;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.scene.SceneApp;

import java.io.IOException;

public class Main {

    public static final CoreLogger LOG = LoggerFactory.get("VkEngine");

    public static void main(String[] args) throws InterruptedException, ConfigParser.ConfigParseException, IOException, SchemaMismatchException {
        EngineCreateInfo createInfo = new EngineCreateInfo("CUBE", "vke");
        createInfo.releaseMode = true;
        createInfo.vulkanCreateInfo.framesInFlight = 3;
        //createInfo.vsync = true;
        createInfo.windowCreateInfo = new WindowCreateInfo("Cube test");

        ConfigurationOption<Boolean> renderdoc = new ConfigurationOption<>("renderdoc", ConfigurationOption.Initializer.BOOLEAN);

        VKEngine engine = new VKEngine(createInfo);

        if (renderdoc.get()) Thread.sleep(5000);

        engine.start(new SceneApp("main"));
    }
}