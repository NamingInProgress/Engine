package com.vke.core.memory;

public interface HeapAllocated<T> {
    T getHeapObject();
    void free();
}
