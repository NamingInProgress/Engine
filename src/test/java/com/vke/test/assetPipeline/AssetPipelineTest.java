package com.vke.test.assetPipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetManager;
import com.vke.api.assets.r.R;
import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.services.Services;

import java.io.IOException;

public class AssetPipelineTest {
    public static void main(String[] args) throws AssetPipelineException, IOException {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");
        VKEngine engine = new VKEngine(createInfo);

        AssetManager manager = engine.service(Services.ASSET_MANAGER);
        PipelineContext pipelineContext = manager.getPipelineContext();
        manager.initialize();

        //AssetHandle<ConfigDocument> testHandle = manager.getAsset("test.json");
        AssetHandle<ConfigDocument> testHandle = R.configs.get("language.en");
        System.out.println(testHandle.acquire(engine));
    }
}
