package com.vke.core.ecs.component;

import com.vke.core.ecs.component.mask.ComponentMask;

public interface Component {
    int ID = -1;
    default int getId() { throw new RuntimeException("stub"); }
    default ComponentMask ofSelf() { return null; }

    default void resize(int newSize) { throw new RuntimeException("stub"); }
    default void swap(int from, int to) { throw new RuntimeException("stub"); }
    default void copyFrom(Component other, int thisIndex, int otherIndex) { throw new RuntimeException("stub"); }
}
