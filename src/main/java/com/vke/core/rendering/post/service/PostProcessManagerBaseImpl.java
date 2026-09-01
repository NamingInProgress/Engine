package com.vke.core.rendering.post.service;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.services2.ScopedServiceImpl;
import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;
import com.vke.core.VKEngine;
import com.vke.core.assets.handles.LazyAssetHandle;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.post.PostEffectProvider;
import com.vke.core.rendering.post.PostProcessEffect;
import com.vke.core.rendering.post.SimplePostProcessEffect;
import com.vke.core.services2.Services;
import com.vke.utils.exception.Unreachable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

public class PostProcessManagerBaseImpl extends ScopedServiceImpl<PostProcessManagerScopedImpl> implements PostProcessManager {
    private final HashMap<Identifier, PostEffectProvider> providers;
    private static final LazyAssetHandle<ConfigSchema> SCHEMA = R.schemas.get("post-stages.vks");

    public PostProcessManagerBaseImpl(VKEngine engine) {
        super(Services.POST_PROCESS, engine);
        this.providers = new HashMap<>();
    }

    @Override
    protected PostProcessManagerScopedImpl createScoped(Context context) {
        return new PostProcessManagerScopedImpl(context, this);
    }

    @Override
    protected void onInitialize() {

    }

    @SuppressWarnings("unchecked")
    void registerStages(Context caller, FileIdentifier vclFile) {
        try {
            ConfigDocument doc = ConfigDocument.parseIdentifier(vclFile);
            doc.validate(SCHEMA.assume(engine), vclFile.toString());

            ConfigNode stagesNode = doc.getRoot().getObject("stages");
            for (ConfigNode stageNode : stagesNode.asArray().values()) {
                String name = stageNode.getString("name");
                Identifier identifier = caller.id(name);

                if ("simple-stage".equals(stageNode.getNodeName())) {
                    String pipelineName = stageNode.getString("pipeline");
                    Identifier pipelineIdent = caller.id(pipelineName);
                    //todo: handle uniforms
                    AssetHandle<? extends RenderPipeline> pipelineHandle = R.pipelines.get(pipelineIdent);

                    class SimpleProvider implements PostEffectProvider {
                        @Override
                        public PostProcessEffect buildEffect(RenderSystem sys, RenderPassInstance renderPass) {
                            return new SimplePostProcessEffect(identifier, sys, renderPass, pipelineHandle);
                        }
                    }

                    providers.put(identifier, new SimpleProvider());
                    //here i need to create instance
                } else if ("custom-stage".equals(stageNode.getNodeName())) {
                    String clasName = stageNode.getString("class");
                    Class<? extends PostProcessEffect> effectClass = (Class<? extends PostProcessEffect>) Class.forName(clasName);
                    Constructor<? extends PostProcessEffect> constructor = effectClass.getDeclaredConstructor(Identifier.class, RenderSystem.class, RenderPassInstance.class);

                    class CustomProvider implements PostEffectProvider {
                        @Override
                        public PostProcessEffect buildEffect(RenderSystem sys, RenderPassInstance renderPass) {
                            try {
                                return constructor.newInstance(identifier, sys, renderPass);
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                engine.throwException(e, "Making new custom post effect: " + identifier);
                                throw new Unreachable();
                            }
                        }
                    }

                    providers.put(identifier, new CustomProvider());
                }
            }

        } catch (IOException | ClassNotFoundException | NoSuchMethodException | SchemaMismatchException e) {
            engine.throwException(e, "PostProcessManager#registerStages");
        }
    }

    @Override
    public PostEffectProvider getEffect(Identifier name) {
        return providers.get(name);
    }

    @Override
    public PostEffectProvider getEffect(String name) {
        return getEffect(engine.id(name));
    }

    @Override
    public List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {

    }
}
