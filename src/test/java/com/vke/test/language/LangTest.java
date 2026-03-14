package com.vke.test.language;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetManager;
import com.vke.api.assets.pipeline.PipelineContext;
import com.vke.api.language.Language;
import com.vke.api.language.LanguageManager;
import com.vke.api.language.LanguageParser;
import com.vke.api.language.Str;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.services.Services;
import com.vke.utils.Location;

import java.io.IOException;
import java.nio.file.Paths;

public class LangTest {
    public static void main(String[] args) throws IOException {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");
        VKEngine engine = new VKEngine(createInfo);

        AssetManager manager = engine.service(Services.ASSET_MANAGER);
        manager.initialize();

        LanguageManager languageManager = engine.service(Services.LANGUAGE_MANAGER);

        Str str = Str.MULTILINGUAL(new Location("culture.food"));

        languageManager.changeLanguage(engine.id("language.en"));
        System.out.println(str.getContents(engine));

        languageManager.changeLanguage(engine.id("language.de"));
        System.out.println(str.getContents(engine));
    }
}
