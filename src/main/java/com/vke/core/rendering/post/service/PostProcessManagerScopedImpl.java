package com.vke.core.rendering.post.service;

import com.vke.core.Context;
import com.vke.core.rendering.post.PostEffectProvider;
import com.vke.core.services2.Services;
import com.vke.utils.io.Identifier;

import java.util.List;

public class PostProcessManagerScopedImpl implements PostProcessManager {
    private final Context context;
    private final PostProcessManagerBaseImpl base;

    public PostProcessManagerScopedImpl(Context context, PostProcessManagerBaseImpl base) {
        this.context = context;
        this.base = base;
    }

    @Override
    public void initialize() {
        base.registerStages(context, context.id("post-stages.vcl"));
    }

    @Override
    public PostEffectProvider getEffect(Identifier name) {
        return base.getEffect(name);
    }

    @Override
    public PostEffectProvider getEffect(String name) {
        return base.getEffect(context.id(name));
    }

    @Override
    public String getId() {
        return base.getId();
    }

    @Override
    public List<String> dependencies() {
        return base.dependencies();
    }

    @Override
    public void free() {

    }
}
