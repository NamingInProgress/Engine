package com.vke.test.asset;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.BundleLoadingCallback;
import com.vke.api.assets.r.R;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.assets.manager.VKEAssetManager;
import com.vke.core.services.Services;

import java.io.IOException;

public class AssetManagerTest {

    public static void main(String[] args) throws IOException {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");

        VKEngine engine = new VKEngine(createInfo);

        VKEAssetManager manager = engine.service(Services.ASSET_MANAGER);
        manager.registerLoadCallback(new BundleLoadingCallback() {
            @Override
            public void onAssetStartLoad(AssetDesc desc) {
                String msg = String.format("[%d/%d] Loading asset %s...", desc.position(), desc.totalAmount(), desc.name().toString());
                System.out.println(msg);
            }

            @Override
            public void onAssetEndLoad(AssetDesc desc) {
                System.out.println("Loading complete!");
            }

            @Override
            public void onAssetException(AssetDesc desc, Throwable exception) {
                engine.throwException(exception, desc.name().toString());
            }
        });

        manager.initialize();

        manager.swapBundle("scene1");

        System.out.println(manager.getAsset("aNum").get());

        AssetHandle<String> test = R.strings.get("something");
        System.out.println(test.acquire(engine));
    }
}
