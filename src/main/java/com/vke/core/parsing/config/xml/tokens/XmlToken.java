package com.vke.core.parsing.config.xml.tokens;

public class XmlToken {
    private final Type type;
    private final Object value;

    public XmlToken(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public XmlToken(Type type) {
        this(type, null);
    }

    public Type getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

    public enum Type {
        LTri,
        RTri,
        Ident,
        StrLit,
        Eq,
        Slash,
        Question,
        Illegal
    }
}
