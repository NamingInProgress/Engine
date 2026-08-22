package com.vke.core.rendering.post.service;

import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.Identifier;
import com.vke.core.rendering.post.PostEffectProvider;

public class PostProcessManagerAPI extends ServiceAPI implements PostProcessManager {
    public PostProcessManagerAPI(ServiceImpl baseImpl) {
        super(baseImpl.getId(), baseImpl);
    }

    private PostProcessManager getImpl() {
        return (PostProcessManager) getImplementation();
    }

    public PostEffectProvider getEffect(Identifier name) {
        return getImpl().getEffect(name);
    }

    public PostEffectProvider getEffect(String name) {
        return getImpl().getEffect(name);
    }
}
