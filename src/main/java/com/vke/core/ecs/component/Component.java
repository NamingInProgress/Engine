package com.vke.core.ecs.component;

import com.vke.core.ecs.ComponentMask;

public interface Component {
    ComponentMask ofSelf();
}
