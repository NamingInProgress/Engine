package com.vke.test;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetManager;
import com.vke.api.assets.r.R;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.assets.VKEAssetManager;
import com.vke.core.services.Services;
import com.vke.utils.Identifier;

import java.io.IOException;

public class AssetManagerTest {

    public static void main(String[] args) throws IOException {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");

        VKEngine engine = new VKEngine(createInfo);

        VKEAssetManager manager = engine.service(Services.ASSET_MANAGER);
        manager.swapBundle("scene1");

        System.out.println(manager.getAsset("aNum").get());

        AssetHandle<String> test = R.strings.get("something");
        System.out.println(test.acquire(engine));
    }
}
