package com.vke.core.scene.env;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.commands.ImmediateSubmit;
import com.vke.api.rendering.abstraction.renderer.data.ImageView;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageUsage;
import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageViewType;
import com.vke.api.rendering.abstraction.renderer.enums.texture.TextureType;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.ValueResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.other.CISResource;
import com.vke.core.Context;
import com.vke.core.assets.meta.AttributedAssetMeta;
import com.vke.core.rendering.Samplers;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.core.rendering.vulkan.texture.VulkanImageView;
import com.vke.core.rendering.vulkan.texture.VulkanTexture;
import com.vke.core.rendering.vulkan.utils.VKUtils;
import com.vke.core.services2.Services;
import com.vke.utils.DrawUtils;
import com.vke.utils.io.Disposable;
import org.lwjgl.vulkan.VK14;

import java.io.IOException;

public class EnvironmentMap implements Disposable {

    private final RenderSystem sys;

    private Texture skybox;
    private ImageView skyboxView;

    public EnvironmentMap(Context context, Texture texture, MapType type) {
        Renderer renderer = context.service(Services.RENDERER);
        RenderSystem renderSys = renderer.renderSystem();
        this(renderSys, texture, type);
    }

    public EnvironmentMap(RenderSystem context, Texture texture, MapType type) {
        this.sys = context;

        if (type == MapType.SPHERICAL) {
            context.runOnNextFrameSync(() -> {
                try {
                    mapSpherical(texture);
                } catch (IOException e) {
                    context.throwException(e, "EnvironmentMap#mapSpherical");
                }
            });
        }
    }

    private void mapSpherical(Texture texture) throws IOException {
        var dev = sys.device();
        var builder = Texture.TextureDesc.builder()
                .width(texture.height() / 2)
                .height(texture.height() / 2)
                .format(texture.format())
                .arrayLayers(6)
                .type(TextureType.TEX_2D)
                .usage(new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.COLOR_ATTACHMENT_BIT, ImageUsage.Bits.TRANSFER_DST_BIT))
                .cubeMap(true);

        this.skybox = dev.createTexture(builder.build());

        AssetHandle<RenderPipeline> e_to_s = R.pipelines.get("vke:equirectangular_to_spherical.pipeline.json");
        RenderPipeline pipeline = e_to_s.acquire(sys);
        CISResource sampler = pipeline.resource("equirectangularMap");
        ValueResource faceResource = pipeline.resource("face");

        ImageView[] views = new ImageView[6];
        for (int face = 0; face < 6; face++) {
            int finalFace = face;
            views[face] = skybox.getView((v) -> {
                v.layerCount(1);
                v.baseLayer(finalFace);
            });
        }

        try (ImmediateSubmit submit = dev.immediateSubmit(QueueType.GRAPHICS)) {
            CommandBuffer cmd = submit.cmd();

            texture.useInShader(cmd);
            sampler.set(texture, Samplers.U_REPEAT_V_CLAMP_LINEAR);

            for (int i = 0; i < views.length; i++) {
                ImageView view = views[i];

                cmd.beginRendering(new CommandBuffer.RenderingInfo(
                        new CommandBuffer.AttachmentInfo(skybox, view, LoadOp.CLEAR, StoreOp.STORE)
                ));

                int finalI = i;
                faceResource.write((writer) -> writer.int1(finalI));

                cmd.bindPipeline(e_to_s);
                cmd.bindDescriptorSets(e_to_s);
                cmd.setPushConstants(e_to_s);

                DrawUtils.fullscreenTri(cmd);

                cmd.endRendering();
            }

            skybox.useInShader(cmd);
        }

//        for (ImageView view : views) {
//            view.free();
//        }

        this.skyboxView = skybox.getView((b) -> {
            b.type(ImageViewType.TEX_CUBE);
        });
    }

    public static EnvironmentMap ofAssetHandle(Context context, AssetHandle<Texture> map) throws IOException {
        MapType mapType = MapType.SPHERICAL;
        if (map.getMeta() instanceof AttributedAssetMeta attribs) {
            try {
                ConfigNode node = attribs.getAttributes().getAssetConfig();
                ConfigNode envMapNode = node.getObject("environment-map");
                String typeName = envMapNode.getStringOption("type").map(String::toUpperCase).unwrapOrNull();
                mapType = MapType.valueOf(typeName);
            } catch (NullPointerException _) {
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(String.format("Illegal map type for environment-map (allowed are 'cUBe' and 'SphEriCaL'): " + e.getMessage()));
            }
        }
        return new EnvironmentMap(context, map.acquire(context), mapType);
    }

    public ImageView getSkyboxView() {
        return skyboxView;
    }

    @Override
    public void free() {
        skybox.free();
    }

    public enum MapType {
        CUBE,
        SPHERICAL
    }
}
