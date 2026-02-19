package com.vke.test;

import com.vke.api.app.Version;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.vkz.VkzArchive;
import com.vke.api.window.WindowCreateInfo;
import com.vke.config.ConfigurationOption;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.services.Services;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.test.app.TestApp;
import com.vke.test.app.TestPipelines;

import java.io.IOException;

public class TestApplication {

    public static void main(String[] args) throws InterruptedException, IOException, ConfigParser.ConfigParseException, SchemaMismatchException {
        //Identifier ident = Identifier.of("schema/layouts.schema.json");
        //char[] source = Utils.readCharsFromInputStream(ident.asInputStream());
        //ConfigParser parser = new JsonParser();
        //parser.setSource(source);
        //ConfigDocument d = parser.parse();
        //ConfigSchema schema = ConfigSchema.readVke(d, ident.getPath());
        //System.out.println(schema);

        ConfigurationOption<Boolean> renderdoc = new ConfigurationOption<>("renderdoc", ConfigurationOption.Initializer.BOOLEAN);

        EngineCreateInfo createInfo = new EngineCreateInfo();
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");

        TestPipelines.init();
        VKEngine engine = new VKEngine(createInfo);
        if (renderdoc.get()) Thread.sleep(5000);

        //Profiler profiler = engine.service(Services.PROFILER);

        engine.start(new TestApp());
        VulkanRenderer renderer = engine.service(Services.VULKAN_RENDERER);
    }

}
