package com.vke.test.asset;

import com.vke.api.assets.AssetHandle;
import com.vke.core.assets.service.AssetManager;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.services2.Services;

import java.io.IOException;

public class AssetProcessorTest {
    public static void main(String[] args) throws IOException {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");

        VKEngine engine = new VKEngine(createInfo);
        AssetManager manager = engine.service(Services.ASSET_MANAGER);
        PipelineContext context = manager.getPipelineContext();
        context.registerProcessor(new HelloWorldProcessor());
        manager.initAssets();

        AssetHandle<String> testHandle = manager.getAsset("test_processed.txt");
        String data = testHandle.acquire(engine);
        System.out.println(data);
    }
}
