package com.vke.core.ecs;

import com.vke.core.ecs.backend.ArchetypeManager;
import com.vke.core.ecs.component.Component;
import org.jetbrains.annotations.Nullable;

public class ComponentReference<T extends Component> {
    private final ArchetypeManager am;
    private final int entity;

    private T component;
    private int i;

    private @Nullable ComponentReference<T> next;
    private @Nullable ComponentProxy<T> proxy;

    public ComponentReference(ArchetypeManager am, int entity) {
        this.entity = entity;
        this.am = am;
    }

    @SuppressWarnings("all")
    public void __0(Component component) {
        this.component = (T) component;
        if (proxy != null) {
            this.proxy.setComponentInternal((T) component);
        }
        if (next != null) next.__0(component);
    }

    public void __1(int i) {
        this.i = i;
        if (proxy != null) {
            this.proxy.setIndexInternal(i);
        }
        if (next != null) next.__1(i);
    }

    public int __2() {
        return component.getId();
    }

    public int getEntity() {
        return entity;
    }

    public T getComponent() {
        if (component == null) {
            throw new IllegalStateException("Entity has been destroyed or this component has been removed from owner");
        }
        return component;
    }

    public int getIndex() {
        return i;
    }

    public ComponentReference<T> createLinked() {
        ComponentReference<T> n = new ComponentReference<>(am, entity);
        ComponentReference<T> parent = this;
        while (parent.next != null) {
            parent = parent.next;
        }
        parent.next = n;
        return n;
    }

    public void linkProxy(ComponentProxy<T> proxy) {
        this.proxy = proxy;
        if (proxy != null) {
            proxy.setComponentInternal(component);
            proxy.setIndexInternal(i);
        }
    }

    public void drop() {
        am.destroyComponentReference(this);
    }
}
