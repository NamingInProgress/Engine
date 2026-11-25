package com.vke.core.rendering.pipeline.driver;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.ImageView;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageAspect;
import com.vke.api.rendering.abstraction.renderer.enums.texture.TextureType;
import com.vke.api.rendering.abstraction.renderer.pipeline.driver.MaterialPipelineDriver;
import com.vke.api.rendering.abstraction.renderer.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.other.CISResource;
import com.vke.core.rendering.Samplers;

import java.io.IOException;

public class DeferredLightPassDriver extends MaterialPipelineDriver {

    private final RenderPipeline p;
    private final CISResource normal, materialIdx, meshUvs, depth, irradianceMap, prefilterMap, brdfLut;
    private final FieldArrayResource lights;
    private final FieldResource lightCount;

    public DeferredLightPassDriver(RenderSystem sys, AssetHandle<? extends Pipeline> pipeline) {
        super(sys, pipeline);
        try {
            this.p = (RenderPipeline) pipeline.acquire(sys);
            this.normal = p.resource("u_NormalTex");
            this.materialIdx = p.resource("u_MaterialIdxTex");
            this.meshUvs = p.resource("u_MeshUvsTex");
            this.depth = p.resource("u_DepthTex");
            this.lightCount = p.resource("u_LightsBuffer.lightCount");
            this.lights = p.resource("u_LightsBuffer.lights");
            this.irradianceMap = p.resource("u_IrradianceMap");
            this.prefilterMap = p.resource("u_PrefilterMap");
            this.brdfLut = p.resource("u_BrdfLut");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(Texture normal, Texture materialIdx, Texture meshUvs, Texture depth, ImageView irradianceMap, ImageView prefilterMap, Texture brdfLut) {
        this.normal.set(normal, Samplers.NEAREST);
        this.materialIdx.set(materialIdx, Samplers.NEAREST);
        this.meshUvs.set(meshUvs, Samplers.NEAREST);
        this.depth.set(depth, Samplers.NEAREST);
        this.irradianceMap.set(irradianceMap, Samplers.LINEAR);
        this.prefilterMap.set(prefilterMap, Samplers.LINEAR);
        this.brdfLut.set(brdfLut, Samplers.LINEAR);
    }

    @Override
    public void use() {
        bind();
        super.use();
        sys.lightManager().write(lights, lightCount);
        bindDescriptorSets();
    }
}
