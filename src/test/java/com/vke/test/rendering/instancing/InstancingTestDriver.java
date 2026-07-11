package com.vke.test.rendering.instancing;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.pipeline.Pipeline;
import com.vke.api.rendering.abstraction.pipeline.PipelineDriver;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.MultiWriteBufferHandle;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.core.Context;
import com.vke.core.rendering.draw.FrameContext;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.ArrayList;

public class InstancingTestDriver extends PipelineDriver {

    private final VulkanRenderPipeline p;
    private final MultiWriteBufferHandle matrixBuffer;
    private final PushConstantHandle projection;

    private final ArrayList<Matrix4f> matrices = new ArrayList<>();

    public InstancingTestDriver(Context context, AssetHandle<? extends Pipeline> pipeline) {
        super(pipeline);
        try {
            this.p = (VulkanRenderPipeline) pipeline.acquire(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.matrixBuffer = p.uniform("ssbo");
        this.projection = p.resolvePushConstant("projection");
    }

    public void addMatrix(Matrix4f mat) {
        matrices.add(mat);
    }

    public void clear() {
        this.matrices.clear();
    }

    public void next() { matrixBuffer.advance(); }

    public void reset() { matrixBuffer.reset(); }

    @Override
    public void use(FrameContext context) {
        bind(context);
        projection.write((slice) -> slice.putMat4(new Matrix4f().setPerspective((float) Math.toRadians(90),
                (float) 800 / 600, 0.1f, 1000, true)));
        matrixBuffer.write(slice -> matrices.forEach(slice::putMat4));
        bindDescriptorSets(context);
        bindPushConstants(context);
    }
}
