package com.vke.core.ecs.backend;

import com.vke.core.ecs.component.Component;
import pl.epsi.EcsComponent;
import pl.epsi.SearchAnnotation;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class ComponentRegistry {
    public static final int INITIAL_COMPONENT_SIZE = 64;

    private static final ArrayList<Component> COMPONENTS;
    private static final ArrayList<Class<? extends Component>> CLASSES;
    private static int COUNTER = 0;

    @SearchAnnotation(target = EcsComponent.class)
    private static final List<Class<? extends Component>> ALL_COMPONENT_CLASSES = List.of();

    static {
        int usedComponents = ALL_COMPONENT_CLASSES.size();
        COMPONENTS = new ArrayList<>(usedComponents);
        CLASSES = new ArrayList<>(usedComponents);

        var lookup = MethodHandles.lookup();
        for (Class<? extends Component> clazz : ALL_COMPONENT_CLASSES) {
            try {
                lookup.ensureInitialized(clazz);
            } catch (Exception ignore) {}
        }
    }


    public static Component getInstance(int id) {
        Component c = COMPONENTS.get(id);
        if (c == null) {
            var clazz = CLASSES.get(id);
            try {
                var constructor = clazz.getDeclaredConstructor(int.class);
                c = constructor.newInstance(INITIAL_COMPONENT_SIZE);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return c;
    }

    public static int register(Class<? extends Component> clazz) {
        int idx = COUNTER++;
        CLASSES.set(idx, clazz);
        return idx;
    }

    public static int getCOUNTER() {
        return COUNTER;
    }
}
