package com.vke.core.rendering.pipeline.driver;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.MaterialPipelineDriver;
import com.vke.api.rendering.abstraction.renderer.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.core.rendering.rp.MeshInstance;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class DeferredPipelineDriver extends MaterialPipelineDriver {

    private final RenderPipeline p;
    private final FieldArrayResource meshInstancesHandle;

    public ArrayList<MeshInstance> meshInstances = new ArrayList<>();

    public DeferredPipelineDriver(RenderSystem context, AssetHandle<? extends Pipeline> pipeline) {
        super(context, pipeline);
        try {
            this.p = (RenderPipeline) pipeline.acquire(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.meshInstancesHandle = p.resource("u_InstanceBuffer.meshInstances");
    }

    @Override
    public void use() {
        for (int i = 0; i < meshInstances.size(); i++) {
            MeshInstance meshInstance = meshInstances.get(i);
            meshInstancesHandle.write(i, meshInstance::putSelf);
        }
        bind();
        bindDescriptorSets();
    }

}
