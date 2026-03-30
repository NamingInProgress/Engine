package com.vke.utils.types;

import com.vke.api.event.Event;

/// DRY code principles compliant code (but only for an event)
public class AbstractSingleValueGetterSetterClass<T> extends Event {
    private T value;

    public void setSingleValue(T newValue) {
        this.value = newValue;
    }

    public T getSingleValue() {
        return value;
    }
}
