package com.vke.test;

import com.vke.Config;
import com.vke.api.app.Version;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.window.WindowCreateInfo;
import com.vke.config.ConfigurationOption;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.parsing.config.json.JsonParser;
import com.vke.core.rendering.vulkan.VulkanRenderer;
import com.vke.core.services.Services;
import com.vke.test.app.TestApp;
import com.vke.utils.Identifier;
import com.vke.utils.Utils;

import java.io.IOException;

public class TestApplication {

    public static void main(String[] args) throws InterruptedException, IOException, ConfigParser.ConfigParseException, SchemaMismatchException {
        Identifier ident = Identifier.of("schema/layouts.schema.json");
        char[] source = Utils.readCharsFromInputStream(ident.asInputStream());
        ConfigParser parser = new JsonParser();
        parser.setSource(source);
        ConfigDocument d = parser.parse();
        ConfigSchema schema = ConfigSchema.readVke(d, ident.getPath());
        System.out.println(schema);

        ConfigurationOption<Boolean> renderdoc = new ConfigurationOption<>("renderdoc", ConfigurationOption.Initializer.BOOLEAN);

        if (renderdoc.get()) Thread.sleep(5000);

        EngineCreateInfo createInfo = new EngineCreateInfo();
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");
        createInfo.vulkanCreateInfo.apiVersion = new Version(1, 0, 3);

        TestPipelines.init();
        VKEngine engine = new VKEngine(createInfo);

        VulkanRenderer renderer = engine.service(Services.VULKAN_RENDERER);

        engine.start(new TestApp());
    }

}
