package com.vke.api.event;

import com.vke.utils.AbstractSingleValueGetterSetterClass;

public abstract class CancellableEvent extends AbstractSingleValueGetterSetterClass<Boolean> {

    public void setCancelled(boolean cancelled) {
        setSingleValue(cancelled);
    }

    public boolean isCancelled() {
        return getSingleValue();
    }

}
