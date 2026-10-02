package com.vke.test.ttf;

import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.vkz.VkzArchive;
import com.vke.api.vkz.VkzEditor;
import com.vke.api.window.WindowCreateInfo;
import com.vke.config.ConfigurationOption;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.scene.SceneApp;
import com.vke.core.services2.Services;
import com.vke.core.vkz.service.Vkz;
import com.vke.utils.Utils;

import java.io.*;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws InterruptedException, ConfigParser.ConfigParseException, IOException, SchemaMismatchException {
        EngineCreateInfo createInfo = new EngineCreateInfo("CUBE", "vke");
        createInfo.releaseMode = true;
        createInfo.vulkanCreateInfo.framesInFlight = 3;
        //createInfo.vsync = true;
        createInfo.windowCreateInfo = new WindowCreateInfo("Cube test");

        ConfigurationOption<Boolean> renderdoc = new ConfigurationOption<>("renderdoc", ConfigurationOption.Initializer.BOOLEAN);

        VKEngine engine = new VKEngine(createInfo);

        Vkz vkz = engine.service(Services.VKZ);
        VkzArchive arch = vkz.createNew();
        VkzEditor regular = arch.root().createFile("JetBrainsMono-Regular.ttf").edit();
        VkzEditor bold = arch.root().createFile("JetBrainsMono-Bold.ttf").edit();

        InputStream regularStream = new FileInputStream("JetBrainsMono-Regular.ttf");
        InputStream boldStream = new FileInputStream("JetBrainsMono-Bold.ttf");

        regular.write(regularStream.readAllBytes());
        regular.commit();
        bold.write(boldStream.readAllBytes());
        bold.commit();

        regularStream.close();
        boldStream.close();

        arch.writeOut(new FileOutputStream( "JetBrainsMono.vkz"));

        if (renderdoc.get()) Thread.sleep(5000);

        engine.start(new SceneApp("main"));
    }

}
