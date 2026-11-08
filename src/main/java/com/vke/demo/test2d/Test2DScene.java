package com.vke.demo.test2d;

import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageUsage;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;
import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.ExrPart;
import com.vke.core.file.exr.extract.ExrScanlineBitmapExtractor;
import com.vke.core.image.Bitmap;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.services2.Services;

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
            FileIdentifier fid = FileIdentifier.of("lol:BrightRings.exr");
            InputStream testImageStream = fid.openInputStream();
            ExrDecoder decoder = new ExrDecoder(testImageStream);
            ExrPart part = decoder.parts[0];
            System.out.println(Arrays.toString(part.offsetTable()));

            System.out.println("channels = " + part.header().chlist.channels);

            ExrScanlineBitmapExtractor ext = new ExrScanlineBitmapExtractor(decoder, testImageStream, 0);
            Bitmap bitmap = ext.readIntoBitmap(false);

            Renderer renderer = context.service(Services.RENDERER);

            var builder = new Texture.TextureDescriptorBuilder()
                    .usage(new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.TRANSFER_SRC_BIT, ImageUsage.Bits.TRANSFER_DST_BIT))
                    .width(part.header().displayWindow.width())
                    .height(part.header().displayWindow.height());

            this.texture = bitmap.createTexture2D(renderer, builder);
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
