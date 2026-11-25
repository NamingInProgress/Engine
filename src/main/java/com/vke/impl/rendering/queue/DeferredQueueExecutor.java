package com.vke.impl.rendering.queue;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.core.color.RgbColor;
import com.vke.core.rendering.graph2.GraphTexture;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.rendering.pipeline.RenderPipelines;
import com.vke.core.rendering.rp.MeshInstance;
import com.vke.core.rendering.rp.queue.SortedRenderQueue;
import com.vke.core.rendering.rp2.queue.RenderQueue;
import com.vke.core.rendering.rp2.queue.RenderQueueExecutor;
import com.vke.core.rendering.rp2.queue.impl.InstancingRenderQueue;
import com.vke.utils.DrawUtils;

import java.util.List;

public class DeferredQueueExecutor extends RenderQueueExecutor<MeshInstance> {

    private GraphTexture colorOut;

    private GraphTexture normal;
    private GraphTexture matIdx;
    private GraphTexture meshUvs;

    private GraphTexture depthOut;

    @Override
    public void onLoad(RenderSystem sys, RenderPass pass) {
        colorOut = pass.searchOutputTexture("colorOut");

        normal = pass.searchOutputTexture("gbuf_normal");
        matIdx = pass.searchOutputTexture("gbuf_material_idx");
        meshUvs = pass.searchOutputTexture("gbuf_mesh_uvs");

        depthOut = pass.searchOutputTexture("depthOut");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void acceptQueue(RenderQueue<MeshInstance> q, CommandBuffer cmd, RenderPass pass) {
        InstancingRenderQueue<MeshInstance> queue = (InstancingRenderQueue<MeshInstance>) q;

        Texture gbuf_normal = normal.extract();
        Texture gbuf_material_idx = matIdx.extract();
        Texture gbuf_mesh_uvs = meshUvs.extract();

        Texture depthOut = this.depthOut.extract();

        pass.beginRendering(cmd, List.of(normal, matIdx, meshUvs), this.depthOut,
                List.of(RgbColor.BLACK, RgbColor.INVALID, RgbColor.BLACK), RgbColor.WHITE);

        RenderPipelines.DEFERRED.upload(queue);
        RenderPipelines.DEFERRED.use();

        for (int i = 0; i < queue.activeKeyCount; i++) {
            int key = queue.activeKeys[i];
            int count = queue.bucketSizes[key];

            MeshInstance first = (MeshInstance) queue.buckets[key][0];
            first.mesh().drawInstanced(count, RenderPipelines.DEFERRED.bucketBaseInstances[key]);
        }

        cmd.endRendering();

        gbuf_normal.useInShader();
        gbuf_material_idx.useInShader();
        gbuf_mesh_uvs.useInShader();
        depthOut.useInShader();

        pass.beginRendering(cmd, List.of(colorOut), RgbColor.BLACK);

        var env = pass.getCurrenScene().getEnvironment().getEnvironmentMap();

        RenderPipelines.DEFERRED_LIGHT_PASS.set(gbuf_normal, gbuf_material_idx, gbuf_mesh_uvs, depthOut,
                env.getIrradianceView(), env.getPrefilterView(), env.getLutMap());
        RenderPipelines.DEFERRED_LIGHT_PASS.use();

        DrawUtils.fullscreenTri(cmd);

        cmd.endRendering();
    }
}
