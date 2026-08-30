package com.vke.core.rendering.pipeline.driver;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.pipeline.MaterialPipelineDriver;
import com.vke.api.rendering.abstraction.renderer.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.PipelineDriver;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.other.CISResource;
import com.vke.core.rendering.Samplers;

import java.io.IOException;

public class DeferredLightPassDriver extends MaterialPipelineDriver {

    private final RenderPipeline p;
    private final CISResource normal, materialIdx, meshUvs, depth;
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(Texture normal, Texture materialIdx, Texture meshUvs, Texture depth) {
        this.normal.set(normal, Samplers.NEAREST);
        this.materialIdx.set(materialIdx, Samplers.NEAREST);
        this.meshUvs.set(meshUvs, Samplers.NEAREST);
        this.depth.set(depth, Samplers.NEAREST);
    }

    @Override
    public void use() {
        super.use();
        sys.lightManager().write(lights, lightCount);
        bind();
        bindDescriptorSets();
    }
}
