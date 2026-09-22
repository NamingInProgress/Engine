package com.vke.impl.rendering.queue;

import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.core.color.RgbColor;
import com.vke.core.rendering.graph2.GraphTexture;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.rendering.pipeline.RenderPipelines;
import com.vke.core.rendering.rp.MeshInstance;
import com.vke.core.rendering.rp.queue.RenderQueue;
import com.vke.core.rendering.rp.queue.RenderQueueExecutor;
import com.vke.utils.DrawUtils;

import java.util.List;

public class DeferredQueueExecutor extends RenderQueueExecutor {

    private GraphTexture colorOut;

    private GraphTexture normal;
    private GraphTexture matIdx;
    private GraphTexture meshUvs;

    private GraphTexture depthOut;

    @Override
    public void onLoad(RenderPass pass) {
        colorOut = pass.searchOutputTexture("colorOut");

        normal = pass.searchOutputTexture("gbuf_normal");
        matIdx = pass.searchOutputTexture("gbuf_material_idx");
        meshUvs = pass.searchOutputTexture("gbuf_mesh_uvs");

        depthOut = pass.searchOutputTexture("depthOut");
    }

    @Override
    public void acceptQueue(RenderQueue queue, CommandBuffer cmd, RenderPass pass) {
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

            MeshInstance first = queue.buckets[key][0];
            first.mesh().drawInstanced(count, RenderPipelines.DEFERRED.bucketBaseInstances[key]);
        }

        cmd.endRendering();

        gbuf_normal.useInShader();
        gbuf_material_idx.useInShader();
        gbuf_mesh_uvs.useInShader();
        depthOut.useInShader();

        pass.beginRendering(cmd, List.of(colorOut), RgbColor.BLACK);

        RenderPipelines.DEFERRED_LIGHT_PASS.set(gbuf_normal, gbuf_material_idx, gbuf_mesh_uvs, depthOut);
        RenderPipelines.DEFERRED_LIGHT_PASS.use();

        DrawUtils.fullscreenTri(cmd);

        cmd.endRendering();
    }
}
