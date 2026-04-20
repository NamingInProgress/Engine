package com.vke.test.language;

import com.vke.core.assets.service.AssetManager;
import com.vke.core.assets.language.service.LanguageManagerBaseImpl;
import com.vke.core.assets.language.Str;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.services2.Services;
import com.vke.utils.io.SegmentedPath;

import java.io.IOException;

public class LangTest {
    public static void main(String[] args) throws IOException {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");
        VKEngine engine = new VKEngine(createInfo);

        AssetManager manager = engine.service(Services.ASSET_MANAGER);
        manager.initAssets();

        LanguageManagerBaseImpl languageManager = engine.service(Services.LANGUAGE_MANAGER);

        Str str = Str.MULTILINGUAL(new SegmentedPath("culture.food"));

        languageManager.changeLanguage(engine.id("language.en"));
        System.out.println(str.getContents(engine));

        languageManager.changeLanguage(engine.id("language.de"));
        System.out.println(str.getContents(engine));
    }
}
