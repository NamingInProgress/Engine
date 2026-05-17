package com.vke.test.asset;

import com.vke.api.assets.AssetHandle;
import com.vke.core.assets.service.AssetManager;
import com.vke.api.assets.r.R;
import com.vke.core.assets.language.Language;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.services2.Services;

import java.io.IOException;

public class AssetPipelineTest {
    public static void main(String[] args) throws AssetException, IOException {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");
        VKEngine engine = new VKEngine(createInfo);

        AssetManager manager = engine.service(Services.ASSET_MANAGER);
        PipelineContext pipelineContext = manager.getPipelineContext();
        manager.initAssets();

        //AssetHandle<ConfigDocument> testHandle = manager.getAsset("test.json");
        AssetHandle<Language> testHandle = R.languages.get("language.en");
        System.out.println(testHandle.acquire(engine));
    }
}
