package com.vke.test.ui;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.assets.handles.utils.LazyAssetHandle;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.ui.rendering.roundrect.RoundRectRenderer;
import com.vke.utils.io.Identifier;

public class SDFRoundRectScene extends Scene {
    private RoundRectRenderer rrr;
    private LazyAssetHandle<Texture> tex = R.textures.get("scaryvulkan.png");

    public SDFRoundRectScene(Identifier name, Context context) {
        super(name, context);
    }

    @Override
    public void onLoad() {
        this.rrr = new RoundRectRenderer(context);
    }

    @Override
    public void onDraw(DrawContext ctx) {
        rrr.beginFrame(ctx);

        //rrr.color(1, 0, 0, 1);
        rrr.texture(tex.assume(context));
        double radiusX = Math.sin((double) System.currentTimeMillis() / 1000);
        radiusX = (radiusX + 1) / 2;
        radiusX *= 100;
        rrr.roundRect(200, 200, 200, 200, (int) radiusX, (int) radiusX);

        rrr.texture(tex.assume(context));
        rrr.strokeRoundRect(500, 200, 200, 200, (int) radiusX, (int) radiusX, 10);

        rrr.draw(ctx);
    }

    @Override
    public void onUnload() {

    }

    @Override
    public void free() {
        rrr.free();
    }
}
