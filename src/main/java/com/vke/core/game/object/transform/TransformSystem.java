package com.vke.core.game.object.transform;

import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.impl.ecs.HierarchyC;
import com.vke.impl.ecs.TransformC;
import com.vke.impl.ecs.WorldTransformC;

public class TransformSystem implements Query {
    @Override
    public ComponentMask getMask() {
        return new ComponentMask(TransformC.ID, WorldTransformC.ID, HierarchyC.ID);
    }

    @Override
    public void execute(Archetype at, int i0, int i1) {
        TransformC t = at.getComponentById(TransformC.ID);
        WorldTransformC w = at.getComponentById(WorldTransformC.ID);
        HierarchyC h = at.getComponentById(HierarchyC.ID);

        for (int i = i0; i < i1; i++) {

        }
    }
}
