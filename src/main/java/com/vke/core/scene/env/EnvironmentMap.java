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
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
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
import com.vke.utils.IntUtils;
import com.vke.utils.Utils;
import com.vke.utils.io.Disposable;
import org.lwjgl.vulkan.VK14;

import java.io.IOException;

public class EnvironmentMap implements Disposable {

    // Must be a power of 2!
    private static final int prefilterMapSize = 256;
    private static final int prefilterMipLevels = IntUtils.fastiLogCeil2(prefilterMapSize);

    private static final AssetHandle<RenderPipeline> e_to_s = R.pipelines.get("vke:equirectangular_to_spherical.pipeline.json");
    private static final AssetHandle<RenderPipeline> irradiance_extraction = R.pipelines.get("vke:irradiance_extraction.pipeline.json");
    private static final AssetHandle<RenderPipeline> prefilter_map_gen = R.pipelines.get("vke:prefilter_map_gen.pipeline.json");
    private static final AssetHandle<RenderPipeline> brdf_convolution = R.pipelines.get("vke:brdf_convolution.pipeline.json");

    private final RenderSystem sys;

    private Texture skybox, irradiance, prefilterMap, lutMap;
    private ImageView skyboxView, irradianceView, prefilterView;

    private final int squareSize;

    public EnvironmentMap(Context context, Texture texture, MapType type) {
        Renderer renderer = context.service(Services.RENDERER);
        RenderSystem renderSys = renderer.renderSystem();
        this(renderSys, texture, type);
    }

    public EnvironmentMap(RenderSystem context, Texture texture, MapType type) {
        this.sys = context;
        this.squareSize = texture.height() / 2;

        try {
            if (type == MapType.SPHERICAL) {
                    mapSpherical(texture);
            }
            createIrradiance();
            createPrefilterMap();
            createBrdfLut();
        } catch (IOException e) {
            context.throwException(e, "EnvironmentMap#init");
        }
    }

    private void mapSpherical(Texture texture) throws IOException {
        var dev = sys.device();
        var builder = Texture.TextureDesc.builder()
                .width(squareSize)
                .height(squareSize)
                .format(Format.RGBA16F)
                .arrayLayers(6)
                .type(TextureType.TEX_2D)
                .usage(new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.COLOR_ATTACHMENT_BIT, ImageUsage.Bits.TRANSFER_DST_BIT))
                .cubeMap(true);

        this.skybox = dev.createTexture(builder.build());

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
            sampler.set(texture, Samplers.LINEAR);

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

        for (ImageView view : views) {
            view.free();
        }

        this.skyboxView = skybox.getViewManaged((b) -> {
            b.type(ImageViewType.TEX_CUBE);
        });
    }

    public void createIrradiance() throws IOException {
        var dev = sys.device();
        var builder = Texture.TextureDesc.builder()
                .width(64)
                .height(64)
                .format(Format.RGBA16F)
                .arrayLayers(6)
                .type(TextureType.TEX_2D)
                .usage(new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.COLOR_ATTACHMENT_BIT, ImageUsage.Bits.TRANSFER_DST_BIT))
                .cubeMap(true);

        this.irradiance = dev.createTexture(builder.build());

        RenderPipeline pipeline = irradiance_extraction.acquire(sys);
        CISResource sampler = pipeline.resource("u_Skybox");
        ValueResource pc = pipeline.resource("face");

        ImageView[] views = new ImageView[6];
        for (int face = 0; face < 6; face++) {
            int finalFace = face;
            views[face] = irradiance.getView((v) -> {
                v.baseLayer(finalFace);
                v.layerCount(1);
            });
        }

        try (ImmediateSubmit s = dev.immediateSubmit(QueueType.GRAPHICS)) {
            CommandBuffer cmd = s.cmd();

            sampler.set(skyboxView, Samplers.LINEAR);

            for (int i = 0; i < views.length; i++) {
                ImageView view = views[i];

                cmd.beginRendering(new CommandBuffer.RenderingInfo(
                        new CommandBuffer.AttachmentInfo(irradiance, view, LoadOp.CLEAR, StoreOp.STORE)
                ));

                int finalI = i;
                pc.write((writer) -> writer.int1(finalI));

                cmd.bindPipeline(irradiance_extraction);
                cmd.bindDescriptorSets(irradiance_extraction);
                cmd.setPushConstants(irradiance_extraction);

                DrawUtils.fullscreenTri(cmd);

                cmd.endRendering();
            }

            irradiance.useInShader(cmd);
        }

        for (ImageView view : views) {
            view.free();
        }

        this.irradianceView = irradiance.getViewManaged((b) -> {
            b.type(ImageViewType.TEX_CUBE);
        });
    }

    public void createPrefilterMap() throws IOException { // TODO: Chatan Jags method for bright spots
        var dev = sys.device();
        var builder = Texture.TextureDesc.builder()
                .width(prefilterMapSize)
                .height(prefilterMapSize)
                .format(Format.RGBA16F)
                .arrayLayers(6)
                .mipLevels(prefilterMipLevels)
                .type(TextureType.TEX_2D)
                .usage(new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.COLOR_ATTACHMENT_BIT, ImageUsage.Bits.TRANSFER_DST_BIT))
                .cubeMap(true);

        this.prefilterMap = dev.createTexture(builder.build());

        RenderPipeline pipeline = prefilter_map_gen.acquire(sys);
        CISResource sampler = pipeline.resource("u_Skybox");
        ValueResource f = pipeline.resource("face");
        ValueResource r = pipeline.resource("roughness");

        ImageView[] views = new ImageView[6 * prefilterMipLevels];
        for (int mip = 0; mip < prefilterMipLevels; mip++) {
            for (int face = 0; face < 6; face++) {
                int finalFace = face;
                int finalMip = mip;
                views[mip * 6 + face] = prefilterMap.getView((v) -> {
                    v.layerCount(1);
                    v.baseLayer(finalFace);
                    v.baseMip(finalMip);
                    v.mipCount(1);
                });
            }
        }

        try (ImmediateSubmit submit = dev.immediateSubmit(QueueType.GRAPHICS)) {
            CommandBuffer cmd = submit.cmd();

            sampler.set(skyboxView, Samplers.LINEAR);

            cmd.bindPipeline(prefilter_map_gen);
            cmd.bindDescriptorSets(prefilter_map_gen);

            for (int mip = 0; mip < prefilterMipLevels; mip++) {
                float roughness = (float) mip / (prefilterMipLevels - 1);
                r.write((writer) -> writer.float1(roughness));

                int size = (int) (Math.pow(0.5, mip) * prefilterMapSize);
                for (int face = 0; face < 6; face++) {
                    cmd.beginRendering(new CommandBuffer.RenderingInfo(
                            size, size,
                            new CommandBuffer.AttachmentInfo(prefilterMap, views[mip * 6 + face], LoadOp.CLEAR, StoreOp.STORE)
                    ));

                    int finalFace = face;
                    f.write((writer) -> writer.int1(finalFace));

                    cmd.setPushConstants(prefilter_map_gen);

                    DrawUtils.fullscreenTri(cmd);

                    cmd.endRendering();
                }
            }

            prefilterMap.useInShader(cmd);
        }

        for (ImageView view : views) {
            view.free();
        }

        this.prefilterView = prefilterMap.getViewManaged((b) -> {
            b.type(ImageViewType.TEX_CUBE);
        });
    }

    public void createBrdfLut() throws IOException {
        var dev = sys.device();
        var builder = Texture.TextureDesc.builder()
                .width(squareSize)
                .height(squareSize)
                .format(Format.RG16F)
                .type(TextureType.TEX_2D)
                .usage(new ImageUsage(ImageUsage.Bits.SAMPLED_BIT, ImageUsage.Bits.COLOR_ATTACHMENT_BIT));

        this.lutMap = dev.createTexture(builder.build());

        try (ImmediateSubmit submit = dev.immediateSubmit(QueueType.GRAPHICS)) {
            CommandBuffer cmd = submit.cmd();

            cmd.beginRendering(new CommandBuffer.RenderingInfo(
                    new CommandBuffer.AttachmentInfo(lutMap, LoadOp.CLEAR, StoreOp.STORE)
            ));

            cmd.bindPipeline(brdf_convolution);

            DrawUtils.fullscreenTri(cmd);

            cmd.endRendering();

            lutMap.useInShader(cmd);
        }

        VKUtils.setDebugName((VulkanRenderSystem) sys, "BRDF_LUT", ((VulkanTexture) lutMap).getHandle(), VK14.VK_OBJECT_TYPE_IMAGE);
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

    public ImageView getIrradianceView() {
        return irradianceView;
    }

    public ImageView getPrefilterView() {
        return prefilterView;
    }

    public Texture getLutMap() {
        return lutMap;
    }

    @Override
    public void free() {
        skybox.free();
        irradiance.free();
        prefilterMap.free();
        lutMap.free();
    }

    public enum MapType {
        CUBE,
        SPHERICAL
    }
}
