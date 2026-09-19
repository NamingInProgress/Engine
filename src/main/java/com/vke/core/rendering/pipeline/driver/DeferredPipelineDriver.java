package com.vke.core.rendering.pipeline.driver;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.MaterialPipelineDriver;
import com.vke.api.rendering.abstraction.renderer.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldResource;
import com.vke.core.rendering.rp.MeshInstance;
import com.vke.core.rendering.rp.queue.RenderQueue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class DeferredPipelineDriver extends MaterialPipelineDriver {

    private final RenderPipeline p;
    private final FieldArrayResource meshInstancesHandle;

    public int[] bucketBaseInstances = new int[64];

    public DeferredPipelineDriver(RenderSystem context, AssetHandle<? extends Pipeline> pipeline) {
        super(context, pipeline);
        try {
            this.p = (RenderPipeline) pipeline.acquire(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.meshInstancesHandle = p.resource("u_InstanceBuffer.meshInstances");
    }

    public void upload(RenderQueue queue) {
        if (bucketBaseInstances.length < queue.buckets.length) {
            bucketBaseInstances = new int[queue.buckets.length];
        }

        int counter = 0;
        for (int i = 0; i < queue.activeKeyCount; i++) {
            int key = queue.activeKeys[i];
            int count = queue.bucketSizes[key];
            MeshInstance[] bucket = queue.buckets[key];

            bucketBaseInstances[key] = counter;

            for (int j = 0; j < count; j++) {
                MeshInstance mi = bucket[j];
                if (mi == null) continue;

                meshInstancesHandle.write(counter++, mi::putSelf);
            }
        }
    }

    @Override
    public void use() {
        bind();
        bindDescriptorSets();
    }
}
