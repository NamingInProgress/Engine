package com.vke.core.ecs.component;

public interface Component {
    int ID = -1;
    default int getId() { throw new RuntimeException("stub"); }
    ComponentMask ofSelf();

    default void resize(int newSize) { throw new RuntimeException("stub"); }
    default void swap(int from, int to) { throw new RuntimeException("stub"); }
    default void copyFrom(Component other, int thisIndex, int otherIndex) { throw new RuntimeException("stub"); }
}
