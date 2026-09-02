package com.vke.core.game.scene.service;

import com.vke.api.services2.PinnedService;
import com.vke.core.game.scene.NodeHierarchy;

public interface HierarchyManager extends PinnedService {
    void updateTransforms();
    NodeHierarchy getHierarchy();
}
