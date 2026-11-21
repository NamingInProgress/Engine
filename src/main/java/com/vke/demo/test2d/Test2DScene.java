package com.vke.demo.test2d;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageTiling;
import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageUsage;
import com.vke.api.rendering.abstraction.renderer.enums.texture.SampleCount;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;
import com.vke.core.color.Color;
import com.vke.core.color.RgbColor;
import com.vke.core.file.deflate.decompress.huffman.HMSymbolDecoder;
import com.vke.core.file.exr.ExrChunkReader;
import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.ExrPart;
import com.vke.core.file.exr.extract.ExrBitmapExtractor;
import com.vke.core.file.exr.extract.ExrScanlineBitmapExtractor;
import com.vke.core.image.Bitmap;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.services2.Services;
import com.vke.utils.Utils;

import java.io.InputStream;
import java.util.Arrays;

public class Test2DScene extends Scene {
    private Texture texture;

    public Test2DScene(Identifier name, Context context) {
        super(name, context);
    }

    @Override
    public void onLoad() throws Exception {
        try {
            if (Utils.FALSE) {
                this.texture = R.textures.get("vke:Ocean.exr").acquire(context);
            } else {
//                this.texture = R.textures.get("vke:png.png").acquire(context);
                this.texture = R.textures.get("vke:Blobbies.exr").acquire(context);
            }

            Renderer renderer = context.service(Services.RENDERER);
            renderer.renderSystem().textureManager().registerTexture(texture);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPrepareRendering(GraphContext context) {
        super.onPrepareRendering(context);
        context.put("texture", texture);
    }

    @Override
    public void free() {

    }
}
