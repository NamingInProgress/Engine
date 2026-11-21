package com.vke.core.rendering.rp.queue;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.assets.r.R;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;
import com.vke.core.assets.handles.LazyAssetHandle;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.rendering.rp.RenderKeyable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class RenderQueueHandler {

    private static final LazyAssetHandle<ConfigSchema> SCHEMA = R.schemas.get("render-queues.vks");
    private static final int VKE_RENDER_QUEUE_COUNT = 2;

    public final ObjectIntHashMap<Identifier> map = new ObjectIntHashMap<>();
    public RenderQueueExecutor<?>[] executors = new RenderQueueExecutor[VKE_RENDER_QUEUE_COUNT];
    public SortedRenderQueue<?>[] queues = new SortedRenderQueue[VKE_RENDER_QUEUE_COUNT];
    private int count = 0;

    @SuppressWarnings("unchecked")
    public <T extends RenderKeyable> void enqueue(int queue, T mi) {
        SortedRenderQueue<T> q = (SortedRenderQueue<T>) queues[queue];
        q.add(mi);
    }

    public void clear() {
        for (SortedRenderQueue<?> queue : queues) {
            queue.clear();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends RenderKeyable> void render(CommandBuffer cmd, RenderPass pass) {
        for (int i = 0; i < executors.length; i++) {
            RenderQueueExecutor<T> executor = (RenderQueueExecutor<T>) executors[i];
            SortedRenderQueue<T> queue = (SortedRenderQueue<T>) queues[i];
            if (executor == null || queue == null) continue;

            executor.acceptQueue(queue, cmd, pass);
        }
    }

    public void onLoad(RenderPass pass) {
        for (RenderQueueExecutor<?> executor : executors) {
            if (executor == null) continue;
            executor.onLoad(pass);
        }
    }

    public void collect(Context ctx) {
        FileIdentifier file = ctx.fid("render-queues.vcl");

        if (!file.existsFile()) return;

        try {
            ConfigDocument doc = ConfigDocument.parseIdentifier(file);
            doc.validate(SCHEMA.assume(ctx), file);

            ConfigNode root = doc.getRoot().getObject("render-queues");
            for (ConfigNode renderQueue : root.asArray().values()) {
                String name = renderQueue.getString("name");
                Identifier ident = ctx.id(name);

                String className = renderQueue.getString("class");
                Class<? extends RenderQueueExecutor<?>> executorClass = (Class<? extends RenderQueueExecutor<?>>) Class.forName(className);

                registerExecutor(ctx, executorClass, ident);
            }
        } catch (IOException | SchemaMismatchException | ClassNotFoundException e) {
            ctx.throwException(e, "RenderQueueCollector#collect");
        }

    }

    private void registerExecutor(Context ctx, Class<? extends RenderQueueExecutor<?>> clazz, Identifier ident) {
        if (executors.length == count) {
            executors = Arrays.copyOf(executors, executors.length + 1);
            queues = Arrays.copyOf(queues, executors.length + 1);
        }

        try {
            var constr = clazz.getConstructor();
            var exec = constr.newInstance();
            executors[count] = exec;
            queues[count] = new SortedRenderQueue<>();
            map.put(ident, count);
            count++;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            ctx.throwException(e, "RenderQueueCollector#registerExecutor");
        }
    }

}
