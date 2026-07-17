package com.vke.core.ecs.backend.archetype;

import com.vke.core.ecs.ComponentMask;

import java.util.Arrays;

public class Archetype {

    private final ComponentMask mask;

    public void resize(int targetSize) {
        x = Arrays.copyOf(x, targetSize);
        y = Arrays.copyOf(y, targetSize);
    }
}