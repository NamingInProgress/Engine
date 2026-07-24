package com.vke.core.scene.loading;

import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.scene.LoadingScene;
import com.vke.core.Context;
import com.vke.utils.io.Identifier;

public class RectLoadingScene extends LoadingScene {

    public RectLoadingScene(Identifier name, Context context) {
        super(name, context);
    }

    @Override
    public void onLoad() throws Exception {
        Renderer renderer = context.service(context.getEngine().rendererType().serviceName);
    }

    @Override
    public void onUnload() throws Exception {

    }

    @Override
    public void onAssetStartLoad(AssetDesc desc) {

    }

    @Override
    public void onAssetEndLoad(AssetDesc desc) {
        //(float) desc.position()) / ((float) desc.totalAmount();
    }

    @Override
    public void onAssetException(AssetDesc desc, Throwable exception) {

    }

    @Override
    public void onLoadingComplete() {
        try {
            completeLoading();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void free() {
    }


}
