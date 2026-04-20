package com.vke.test.asset;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.BundleExchange;
import com.vke.api.assets.r.R;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.manager.VKEAssetManager;
import com.vke.core.services2.Services;

import java.io.IOException;

public class AssetManagerTest {

    public static void main(String[] args) throws IOException, AssetException {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");

        VKEngine engine = new VKEngine(createInfo);
        Context lolCtx = engine.createNewContext("lol");

        VKEAssetManager vkeManager = engine.service(Services.ASSET_MANAGER);
        VKEAssetManager lolManager = lolCtx.service("asm");

        vkeManager.initialize();
        lolManager.initialize();

        BundleExchange exchange = vkeManager.beginExchange();
        exchange.load("scene1");
        exchange.commit();

        AssetHandle<String> greeting1 = lolManager.getAsset("vke:something");
        AssetHandle<String> greeting2 = R.strings.get("lol:something2");

        System.out.println(greeting1.acquire(engine));
        System.out.println(greeting2.acquire(engine));
    }
}
