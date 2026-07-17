package com.vke.test.asset;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.core.Context;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProcessor;

public class HelloWorldProcessor implements AssetProcessor {
    @Override
    public String getName() {
        return "helloworld";
    }

    @Override
    public void process(Context context, StageElement input, AssetData resolvedData, ConfigArrayNode arguments) {
        String data = resolvedData.getDataAs();
        data = "HelloWorld!" + System.lineSeparator() + data;
        input.setData(AssetData.plain(data));
    }
}
