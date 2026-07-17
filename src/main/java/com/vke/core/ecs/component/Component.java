package com.vke.core.ecs.component;

import com.vke.core.ecs.component.mask.ComponentMask;

public interface Component {
    int getId();
    ComponentMask ofSelf();

    void makeRoom(int amount);
    void swap(int a, int b);
}
