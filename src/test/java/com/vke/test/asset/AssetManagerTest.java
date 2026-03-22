package com.vke.test.asset;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.BundleLoadingCallback;
import com.vke.api.assets.r.R;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.Context;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.assets.manager.VKEAssetManager;
import com.vke.core.services.Services;
import com.vke.core.vulkan.VulkanRenderer;

import java.io.IOException;

public class AssetManagerTest {

    public static void main(String[] args) throws IOException {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");

        VKEngine engine = new VKEngine(createInfo);
        Context lolCtx = engine.createNewContext("lol");

        VKEAssetManager vkeManager = engine.service(Services.ASSET_MANAGER);
        VKEAssetManager lolManager = lolCtx.service("asm");

        vkeManager.initialize();
        lolManager.initialize();

        vkeManager.swapBundle("scene1");

        AssetHandle<String> greeting = lolManager.getAsset("vke:greeting");
        AssetHandle<String> string = lolManager.getAsset("string");

        System.out.println(greeting.acquire(engine));
        System.out.println(string.acquire(engine));
    }
}
