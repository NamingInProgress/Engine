package com.vke.core.parsing.config.json.tokens;

public class JsonToken {
    private final Object value;
    private final Type type;

    public JsonToken(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public JsonToken(Type type) {
        this(type, null);
    }

    public Type getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public <T> T value() {
        return (T) value;
    }

    public enum Type {
        LBrace,
        RBrace,
        LBrack,
        RBrack,
        Colon,
        Comma,
        NumLit,
        StrLit,
    }
}
