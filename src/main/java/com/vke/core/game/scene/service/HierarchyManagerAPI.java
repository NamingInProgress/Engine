package com.vke.core.game.scene.service;

import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.game.scene.NodeHierarchy;

public class HierarchyManagerAPI extends ServiceAPI implements HierarchyManager {
    public HierarchyManagerAPI(ServiceImpl baseImpl) {
        super(baseImpl.getId(), baseImpl);
    }

    private HierarchyManager getImpl() {
        return (HierarchyManager) getImplementation();
    }

    @Override
    public void updateTransforms() {
        getImpl().updateTransforms();
    }

    @Override
    public NodeHierarchy getHierarchy() {
        return getImpl().getHierarchy();
    }
}
