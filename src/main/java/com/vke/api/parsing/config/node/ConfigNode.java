package com.vke.api.parsing.config.node;

public interface ConfigNode {
    Type getType();

    enum Type {
        Object,
        Array,
        Number,
        Value,
        Boolean,
        Meta,
    }
}
