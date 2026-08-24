package com.vke.test.jpeg;

import com.vke.api.assets.AssetHandle;
import com.vke.api.game.camera.Camera;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.assets.service.AssetManager;
import com.vke.core.game.camera.OriginOrthoCamera;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.services2.Services;

public class JpegTestScene extends Scene {
    private AssetHandle<Texture> texture;

    public JpegTestScene(Identifier name, Context context) {
        super(name, context);
    }

    @Override
    public void onLoad() throws Exception {
        Camera camera = new OriginOrthoCamera(context);
        camera.use();

        AssetManager assets = context.service(Services.ASSET_MANAGER);
        assets.initAssets();

        texture = assets.getAsset("scaryvulkan.png");
    }

    @Override
    public void onPrepareRendering(GraphContext context) {
        int width = 500, height = 500;
        int[] rect = { -width/2, -height/2, width, height };
        context.put("tex", texture);
        context.put("rect", rect);
    }

    @Override
    public void free() {

    }
}
