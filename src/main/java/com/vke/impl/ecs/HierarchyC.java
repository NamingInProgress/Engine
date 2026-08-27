package com.vke.impl.ecs;

import com.vke.core.ecs.component.Component;
import pl.epsi.EcsComponent;

@EcsComponent
public class HierarchyC implements Component {
    public int[] parent;

    @Override
    public void initialize(int i) {
        parent[i] = -1;
    }
}
