package com.vke.core.rendering.rp2.queue;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.assets.r.R;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;
import com.vke.core.assets.handles.LazyAssetHandle;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.rendering.rp.MeshInstance;
import com.vke.core.rendering.rp.RenderKeyable;
import com.vke.core.services2.Services;
import com.vke.utils.exception.Unreachable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;

public class RenderQueueCollector {

    private static final LazyAssetHandle<ConfigSchema> RENDER_QUEUES_SCHEMA = R.schemas.get("render-queues.vks");
    private static final LazyAssetHandle<ConfigSchema> RENDER_QUEUE_PROVIDERS_SCHEMA = R.schemas.get("render-queue-providers.vks");

    private final ObjectIntHashMap<Identifier> nameToIndex = new ObjectIntHashMap<>();
    private final HashMap<Identifier, RenderQueueProvider> nameToQueue = new HashMap<>();

    private CombinedQueue<?>[] queues = new CombinedQueue[0];
    private int maxQueue;

    private final RenderSystem sys;

    public RenderQueueCollector(Context ctx) {
        collectProviders(ctx);
        collectQueues(ctx);

        Renderer renderer = ctx.service(Services.RENDERER);
        this.sys = renderer.renderSystem();
    }

    public <T> void enqueue(Identifier queueIdent, T inst) {
        enqueue(getIndex(queueIdent), inst);
    }

    @SuppressWarnings("unchecked")
    public <T> void enqueue(int queueIdx, T inst) {
        RenderQueue<T> queue = (RenderQueue<T>) queues[queueIdx].queue;
        queue.add(inst);
    }

    @SuppressWarnings("unchecked")
    public <T> void render(CommandBuffer cmd, RenderPass pass) {
        for (int i = 0; i < maxQueue; i++) {
            CombinedQueue<T> queue = (CombinedQueue<T>) queues[i];
            queue.executor.acceptQueue(queue.queue, cmd, pass);
        }
    }

    public void clear() {
        for (int i = 0; i < maxQueue; i++) {
            CombinedQueue<?> queue = queues[i];
            queue.queue.clear();
        }
    }

    public void onLoad(RenderPass pass) {
        for (int i = 0; i < maxQueue; i++) {
            CombinedQueue<?> queue = queues[i];
            queue.executor.onLoad(sys, pass);
        }
    }

    @SuppressWarnings("unchecked")
    public void collectProviders(Context ctx) {
        FileIdentifier file = ctx.fid("render-queue-providers.vcl");

        if (!file.existsFile()) return;

        try {
            ConfigDocument doc = ConfigDocument.parseIdentifier(file);
            doc.validate(RENDER_QUEUE_PROVIDERS_SCHEMA.assume(ctx), file);

            ConfigNode root = doc.getRoot().getObject("queue-providers");
            for (ConfigNode provider : root.asArray().values()) {
                String name = provider.getString("name");
                Identifier ident = ctx.id(name);

                String className = provider.getString("class");
                Class<? extends RenderQueueProvider> providerClass =
                        (Class<? extends RenderQueueProvider>) Class.forName(className);

                RenderQueueProvider p;
                try {
                    p = providerClass.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | NoSuchMethodException |
                         IllegalAccessException e) {
                    ctx.throwException(e, "RenderQueueCollector#ProvidingQueue");
                    throw new Unreachable();
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e.getTargetException());
                }

                nameToQueue.put(ident, p);
            }
        } catch (IOException | SchemaMismatchException | ClassNotFoundException e) {
            ctx.throwException(e, "RenderQueueCollector#collectProviders");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void collectQueues(Context ctx) {
        FileIdentifier file = ctx.fid("render-queues.vcl");

        if (!file.existsFile()) return;

        try {
            ConfigDocument doc = ConfigDocument.parseIdentifier(file);
            doc.validate(RENDER_QUEUES_SCHEMA.assume(ctx), file);

            ConfigNode root = doc.getRoot().getObject("render-queues");
            for (ConfigNode renderQueue : root.asArray().values()) {
                String name = renderQueue.getString("name");
                Identifier ident = ctx.id(name);

                String className = renderQueue.getString("class");
                String type = renderQueue.getString("type");
                Identifier typeIdent = ctx.id(type);
                Class<? extends RenderQueueExecutor<?>> executorClass =
                        (Class<? extends RenderQueueExecutor<?>>) Class.forName(className);

                RenderQueueExecutor<T> exec = (RenderQueueExecutor<T>) executorClass.getDeclaredConstructor().newInstance();
                RenderQueue<T> queue = (RenderQueue<T>) nameToQueue.get(typeIdent).get();

                registerQueue(ident, exec, queue);
            }
        } catch (IOException | SchemaMismatchException | ClassNotFoundException | NoSuchMethodException |
                 InstantiationException | IllegalAccessException | InvocationTargetException e) {
            ctx.throwException(e, "RenderQueueCollector#collectQueue");
        }
    }

    public <T> void registerQueue(Identifier queueIdent, RenderQueueExecutor<T> executor, RenderQueue<T> queue) {
        if (maxQueue == queues.length) {
            queues = Arrays.copyOf(queues, queues.length == 0 ? 2 : queues.length * 2);
        }

        queues[maxQueue] = new CombinedQueue<T>(queue, executor);
        nameToIndex.put(queueIdent, maxQueue);
        maxQueue++;
    }

    // region Getters
    @SuppressWarnings("unchecked")
    public <T extends RenderQueueExecutor<?>> T getExecutor(int idx) {
        return (T) queues[idx].executor;
    }

    @SuppressWarnings("unchecked")
    public <T extends RenderQueue<?>> T getQueue(int idx) {
        return (T) queues[idx].queue;
    }

    @SuppressWarnings("unchecked")
    public <T extends CombinedQueue<?>> T getCombined(int idx) {
        if (idx == -1) throw new IllegalStateException("Unknown render queue with index: `" + idx + "`");
        return (T) queues[idx];
    }

    public <T extends RenderQueueExecutor<?>> T getExecutor(Identifier identifier) {
        return getExecutor(getIndex(identifier));
    }

    public <T extends RenderQueue<?>> T getQueue(Identifier identifier) {
        return getQueue(getIndex(identifier));
    }

    public <T extends CombinedQueue<?>> T getCombined(Identifier identifier) {
        return getCombined(getIndex(identifier));
    }

    public int getIndex(Identifier ident) {
        return nameToIndex.getOrDefault(ident, -1);
    }
    // endregion

    public record CombinedQueue<T>(RenderQueue<T> queue, RenderQueueExecutor<T> executor) {}

}
