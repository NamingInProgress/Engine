package com.vke.core.game.scene.service;

import com.vke.api.services2.ServiceImpl;
import com.vke.core.VKEngine;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.game.scene.NodeHierarchy;
import com.vke.core.services2.Services;

import java.util.List;

public class HierarchyManagerImpl extends ServiceImpl implements HierarchyManager {
    private NodeHierarchy hierarchy;

    public HierarchyManagerImpl(VKEngine engine) {
        super(Services.HIERARCHY, engine);
    }

    @Override
    protected void onInitialize() {
        EcsManager ecs = engine.service(Services.ECS);
        hierarchy = new NodeHierarchy(engine, ecs);
    }

    @Override
    public void updateTransforms() {
        hierarchy.updateTransforms();
    }

    @Override
    public NodeHierarchy getHierarchy() {
        return hierarchy;
    }

    @Override
    public List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {

    }
}
