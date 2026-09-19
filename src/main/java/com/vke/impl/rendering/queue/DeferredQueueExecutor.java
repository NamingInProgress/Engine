package com.vke.impl.rendering.queue;

import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.color.RgbColor;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.pipeline.RenderPipelines;
import com.vke.core.rendering.rp.MeshInstance;
import com.vke.core.rendering.rp.queue.RenderQueue;
import com.vke.core.rendering.rp.queue.RenderQueueExecutor;
import com.vke.utils.DrawUtils;

import java.util.List;

public class DeferredQueueExecutor extends RenderQueueExecutor {
    @Override
    public void acceptQueue(RenderQueue queue, CommandBuffer cmd, RenderPass pass, RenderPassInstance instance) {
        Texture gbuf_normal = instance.getOutputTexture("gbuf_normal");
        Texture gbuf_material_idx = instance.getOutputTexture("gbuf_material_idx");
        Texture gbuf_mesh_uvs = instance.getOutputTexture("gbuf_mesh_uvs");

        Texture depthOut = instance.getOutputTexture("depthOut");

        pass.beginRendering(cmd, List.of("gbuf_normal", "gbuf_material_idx", "gbuf_mesh_uvs"), "depthOut",
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

        pass.beginRendering(cmd, List.of("colorOut"), RgbColor.BLACK);

        gbuf_normal.useInShader();
        gbuf_material_idx.useInShader();
        gbuf_mesh_uvs.useInShader();
        depthOut.useInShader();

        RenderPipelines.DEFERRED_LIGHT_PASS.set(gbuf_normal, gbuf_material_idx, gbuf_mesh_uvs, depthOut);
        RenderPipelines.DEFERRED_LIGHT_PASS.use();

        DrawUtils.fullscreenTri(cmd);

        cmd.endRendering();
    }
}
