package com.vke.core.ecs.component;

public interface Component {
    int getId();
    ComponentMask ofSelf();

    void makeRoom(int amount);
    void swap(int a, int b);
    void copyFrom(Component other, int otherIndex, int thisIndex);
}
