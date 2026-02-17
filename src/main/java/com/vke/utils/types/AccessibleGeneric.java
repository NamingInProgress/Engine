package com.vke.utils.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

public abstract class AccessibleGeneric<T> {
    private final Type type;

    protected AccessibleGeneric() {
        ParameterizedType pt =
                (ParameterizedType) getClass().getGenericSuperclass();
        this.type = pt.getActualTypeArguments()[0];
    }

    protected Type getGeneric() {
        return type;
    }
}
