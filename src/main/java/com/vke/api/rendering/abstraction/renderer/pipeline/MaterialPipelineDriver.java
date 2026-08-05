package com.vke.api.rendering.abstraction.renderer.pipeline;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.MaterialManager;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;

public abstract class MaterialPipelineDriver extends PipelineDriver {

    protected final MaterialManager materialManager;
    protected final FieldArrayResource u_MaterialBuffer;

    public MaterialPipelineDriver(RenderSystem sys, AssetHandle<? extends Pipeline> pipeline) {
        super(sys, pipeline);
        this.materialManager = sys.materialManager();
        this.u_MaterialBuffer = p.resource("u_MaterialBuffer.materials");
    }

    @Override
    public void bindDescriptorSets() {
        materialManager.upload(u_MaterialBuffer);
        super.bindDescriptorSets();
    }
}
