package com.vke.core.ecs;

import com.vke.core.ecs.component.Component;

public abstract class ComponentProxy<T extends Component> {
    public abstract void setComponentInternal(T component);
    public abstract void setIndexInternal(int index);
}
