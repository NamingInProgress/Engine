package com.vke.test.jpeg;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.utils.io.Identifier;

public class JpegTestScene extends Scene {
    private AssetHandle<Texture> texture;

    public JpegTestScene(Identifier name, Context context) {
        super(name, context);
    }

    @Override
    public void onLoad() throws Exception {

    }

    @Override
    public void onPrepareRendering(GraphContext context) {
        context.put("tex", texture);
    }

    @Override
    public void free() {

    }
}
