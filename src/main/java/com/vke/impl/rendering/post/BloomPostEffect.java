package com.vke.impl.rendering.post;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.other.CISResource;
import com.vke.core.Identifier;
import com.vke.core.rendering.Samplers;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.graph.def.PostRenderPassDefinition;
import com.vke.core.rendering.graph.def.RenderPassDefinition;
import com.vke.core.rendering.post.PostProcessEffect;
import com.vke.utils.DrawUtils;

import java.io.IOException;
import java.util.List;

public class BloomPostEffect extends PostProcessEffect {

    private final AssetHandle<RenderPipeline> highlightExtractHandle = R.pipelines.get("vke:bloom_highlight_extract.pipeline.json");
    private RenderPipeline highlightExtract;
    private CISResource highlightInTexture;

    private final AssetHandle<RenderPipeline> downscaleHandle = R.pipelines.get("vke:bloom_downscale.pipeline.json");
    private RenderPipeline downscale;
    private CISResource downscaleInTexture;

    private final AssetHandle<RenderPipeline> upscaleHandle = R.pipelines.get("vke:bloom_upscale.pipeline.json");
    private RenderPipeline upscale;
    private CISResource upscaleInTexture;

    private final AssetHandle<RenderPipeline> combineHandle = R.pipelines.get("vke:bloom_combine.pipeline.json");
    private RenderPipeline combine;
    private CISResource combineInOriginal;
    private CISResource combineInBloom;

    public BloomPostEffect(Identifier identifier, RenderSystem renderSystem, RenderPassInstance instance) {
        super(identifier, renderSystem, instance);

        if (instance.getDefinition() instanceof PostRenderPassDefinition prpd) {
            prpd.outputs().add(new RenderPassDefinition.OutputTextureDefinition(
                    "bloomHighlightExtracted", null, RenderPassDefinition.TextureType.COLOR, Format.RGBA16F, 0, 0, 1
            ));
            prpd.outputs().add(new RenderPassDefinition.OutputTextureDefinition(
                    "bloomDownsample1", null, RenderPassDefinition.TextureType.COLOR, Format.RGBA16F, 0, 0, 0.5f
            ));
            prpd.outputs().add(new RenderPassDefinition.OutputTextureDefinition(
                    "bloomDownsample2", null, RenderPassDefinition.TextureType.COLOR, Format.RGBA16F, 0, 0, 0.25f
            ));
            prpd.outputs().add(new RenderPassDefinition.OutputTextureDefinition(
                    "bloomDownsample3", null, RenderPassDefinition.TextureType.COLOR, Format.RGBA16F, 0, 0, 0.125f
            ));
            prpd.outputs().add(new RenderPassDefinition.OutputTextureDefinition(
                    "bloomDownsample4", null, RenderPassDefinition.TextureType.COLOR, Format.RGBA16F, 0, 0, 0.0625f
            ));
        } else {
            renderSystem.throwException(new IllegalArgumentException("Bloom post effect included in render pass instance who's definition is not a PostRenderPassDefinition"), "Bloom Post Effect");
        }
    }

    @Override
    public boolean autoStartRendering() {
        return false;
    }

    @Override
    public void onInitialize() {
        try {
            this.highlightExtract = highlightExtractHandle.acquire(renderSystem);
            this.highlightInTexture = highlightExtract.resource("u_InTex");

            this.downscale = downscaleHandle.acquire(renderSystem);
            this.downscaleInTexture = downscale.resource("u_InTex");

            this.upscale = upscaleHandle.acquire(renderSystem);
            this.upscaleInTexture = upscale.resource("u_InTex");

            this.combine = combineHandle.acquire(renderSystem);
            this.combineInOriginal = combine.resource("u_Original");
            this.combineInBloom = combine.resource("u_Blurred");
        } catch (IOException e) {
            renderSystem.throwException(e, "Bloom Post Effect");
        }
    }

    @Override
    public void draw(CommandBuffer cmd, GraphContext ctx, Texture colorInput, Texture colorOutput) {
        Texture highlighted = instance.getOutputTexture("bloomHighlightExtracted");
        Texture downsample1 = instance.getOutputTexture("bloomDownsample1");
        Texture downsample2 = instance.getOutputTexture("bloomDownsample2");
        Texture downsample3 = instance.getOutputTexture("bloomDownsample3");
        Texture downsample4 = instance.getOutputTexture("bloomDownsample4");

        // HIGHLIGHT
        cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
                new CommandBuffer.AttachmentInfo(highlighted, LoadOp.CLEAR, StoreOp.STORE, new float[]{0.2f, 0.3f, 0.3f, 1.0f})
        ), null));

        cmd.bindPipeline(highlightExtractHandle);
        highlightInTexture.set(colorInput, Samplers.LINEAR);
        cmd.bindDescriptorSets(highlightExtractHandle);
        DrawUtils.fullscreenTri(cmd);

        cmd.endRendering();

        // DS1
        highlighted.useInShader();

        cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
                new CommandBuffer.AttachmentInfo(downsample1, LoadOp.CLEAR, StoreOp.STORE, new float[]{0.2f, 0.3f, 0.3f, 1.0f})
        ), null));

        cmd.bindPipeline(downscaleHandle);
        downscaleInTexture.set(highlighted, Samplers.LINEAR);
        cmd.bindDescriptorSets(downscaleHandle);
        DrawUtils.fullscreenTri(cmd);

        cmd.endRendering();

        // DS2
        downsample1.useInShader();

        cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
                new CommandBuffer.AttachmentInfo(downsample2, LoadOp.CLEAR, StoreOp.STORE, new float[]{0.2f, 0.3f, 0.3f, 1.0f})
        ), null));

        cmd.bindPipeline(downscaleHandle);
        downscaleInTexture.nextWrite();
        downscaleInTexture.set(downsample1, Samplers.LINEAR);
        cmd.bindDescriptorSets(downscaleHandle);
        DrawUtils.fullscreenTri(cmd);

        cmd.endRendering();

//        // DS3
//        downsample2.useInShader();
//
//        cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
//                new CommandBuffer.AttachmentInfo(downsample3, LoadOp.CLEAR, StoreOp.STORE, new float[]{0.2f, 0.3f, 0.3f, 1.0f})
//        ), null));
//
//        cmd.bindPipeline(downscaleHandle);
//        downscaleInTexture.nextWrite();
//        downscaleInTexture.set(downsample2, Samplers.LINEAR);
//        cmd.bindDescriptorSets(downscaleHandle);
//        DrawUtils.fullscreenTri(cmd);
//
//        cmd.endRendering();
//
//        // DS4
//        downsample3.useInShader();
//
//        cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
//                new CommandBuffer.AttachmentInfo(downsample4, LoadOp.CLEAR, StoreOp.STORE, new float[]{0.2f, 0.3f, 0.3f, 1.0f})
//        ), null));
//
//        cmd.bindPipeline(downscaleHandle);
//        downscaleInTexture.nextWrite();
//        downscaleInTexture.set(downsample3, Samplers.LINEAR);
//        cmd.bindDescriptorSets(downscaleHandle);
//        DrawUtils.fullscreenTri(cmd);
//
//        cmd.endRendering();
//
//        downsample4.useInShader();
//
//        // UPSCALE 4->3
//        cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
//                new CommandBuffer.AttachmentInfo(downsample3, LoadOp.LOAD, StoreOp.STORE, new float[]{0.2f, 0.3f, 0.3f, 1.0f})
//        ), null));
//
//        cmd.bindPipeline(upscaleHandle);
//        upscaleInTexture.set(downsample4, Samplers.LINEAR);
//        cmd.bindDescriptorSets(upscaleHandle);
//        DrawUtils.fullscreenTri(cmd);
//
//        cmd.endRendering();
//
//        // UPSCALE 3->2
//        downsample3.useInShader();
//
//        cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
//                new CommandBuffer.AttachmentInfo(downsample2, LoadOp.LOAD, StoreOp.STORE, new float[]{0.2f, 0.3f, 0.3f, 1.0f})
//        ), null));
//
//        cmd.bindPipeline(upscaleHandle);
//        upscaleInTexture.nextWrite();
//        upscaleInTexture.set(downsample3, Samplers.LINEAR);
//        cmd.bindDescriptorSets(upscaleHandle);
//        DrawUtils.fullscreenTri(cmd);
//
//        cmd.endRendering();

        // UPSCALE 2->1
        downsample2.useInShader();

        cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
                new CommandBuffer.AttachmentInfo(downsample1, LoadOp.LOAD, StoreOp.STORE, new float[]{0.2f, 0.3f, 0.3f, 1.0f})
        ), null));

        cmd.bindPipeline(upscaleHandle);
        upscaleInTexture.nextWrite();
        upscaleInTexture.set(downsample2, Samplers.LINEAR);
        cmd.bindDescriptorSets(upscaleHandle);
        DrawUtils.fullscreenTri(cmd);

        cmd.endRendering();

        // COMBINE
        downsample1.useInShader();

        cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
                new CommandBuffer.AttachmentInfo(colorOutput, LoadOp.CLEAR, StoreOp.STORE, new float[]{0.2f, 0.3f, 0.3f, 1.0f})
        ), null));

        cmd.bindPipeline(combineHandle);
        combineInBloom.set(downsample1, Samplers.LINEAR);
        combineInOriginal.set(colorInput, Samplers.LINEAR);
        cmd.bindDescriptorSets(combineHandle);
        DrawUtils.fullscreenTri(cmd);

        cmd.endRendering();
    }

}
