package com.vke.core.ecs.backend;

import com.vke.core.ecs.component.Component;

public class ComponentRegistry {
    public Component getInstance(int id) {
        return null;
    }

    public static int register(Class<? extends Component> clazz) {
        throw new RuntimeException("stub");
    }
}
