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
import com.vke.core.file.deflate.decompress.huffman.HMSymbolDecoder;
import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.ExrPart;
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
            FileIdentifier fid = FileIdentifier.of("lol:Blobbies.exr");
            InputStream testImageStream = fid.openInputStream();
            ExrDecoder decoder = new ExrDecoder(testImageStream);
            ExrPart part = decoder.parts[0];
            System.out.println(Arrays.toString(part.offsetTable()));

            System.out.println("channels = " + part.header().chlist);

            ExrScanlineBitmapExtractor ext = new ExrScanlineBitmapExtractor(decoder, testImageStream, 0);

            HMSymbolDecoder.LOOKUP = 0;
            HMSymbolDecoder.TREE = 0;
            Bitmap bitmap = ext.readIntoBitmap(false, context.getEngine());
            System.out.println("lookup: " + HMSymbolDecoder.LOOKUP + ", tree: " + HMSymbolDecoder.TREE);

            Renderer renderer = context.service(Services.RENDERER);

            var builder = new Texture.TextureDescriptorBuilder()
                    .sampleCount(SampleCount.X1)
                    .mipLevels(1)
                    .tiling(ImageTiling.OPTIMAL)
                    .usage(new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.TRANSFER_SRC_BIT, ImageUsage.Bits.TRANSFER_DST_BIT))
                    .width(part.header().displayWindow.width())
                    .height(part.header().displayWindow.height());

            if (Utils.TRUE) {
                this.texture = bitmap.createTexture2D(renderer, builder);
            } else {
                this.texture = R.textures.get("vke:png.png").acquire(context);
            }
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
