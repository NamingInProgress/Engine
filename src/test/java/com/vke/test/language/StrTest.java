package com.vke.test.language;

import com.vke.api.assets.AssetManager;
import com.vke.core.assets.language.Str;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.services2.Services;

public class StrTest {
    public static void main(String[] args) {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");

        VKEngine engine = new VKEngine(createInfo);
        AssetManager manager = engine.service(Services.ASSET_MANAGER);
        manager.initialize();

        Str test1 = Str.STATIC("Hello World!");
        Str test2 = Str.ASSET(engine.id("greeting"));
        System.out.println(test1.getContents(engine));
        System.out.println(test2.getContents(engine));
    }
}
