package com.vke.core.rendering.post.service;

import com.vke.api.services2.PinnedService;
import com.vke.core.Identifier;
import com.vke.core.rendering.post.PostEffectProvider;

public interface PostProcessManager extends PinnedService {
    void initialize();
    PostEffectProvider getEffect(Identifier name);
    PostEffectProvider getEffect(String name);
}
