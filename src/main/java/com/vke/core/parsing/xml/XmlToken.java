package com.vke.core.parsing.xml;

import com.vke.api.parsing.Token;
import com.vke.api.parsing.TokenType;
import com.vke.api.parsing.Tokenizer;

public class XmlToken implements Token<XmlToken.Type> {
    private int line, pos;
    private Type type;
    private Object value;

    public XmlToken(int line, int pos, Type type, Object value) {
        this.line = line;
        this.pos = pos;
        this.type = type;
        this.value = value;
    }

    public XmlToken(int line, int pos, Type type) {
        this(line, pos, type, null);
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getPosition() {
        return pos;
    }

    @Override
    public Type getType() {
        return type;
    }

    public String asString() throws Tokenizer.TokenException {
        try {
            return (String) value;
        } catch (ClassCastException e) {
            throw new Tokenizer.TokenException(line, pos, "Tried to get string from " + type);
        }
    }

    public int asInt() throws Tokenizer.TokenException {
        try {
            return (int) value;
        } catch (ClassCastException e) {
            throw new Tokenizer.TokenException(line, pos, "Tried to get int from " + type);
        }
    }

    public float asFloat() throws Tokenizer.TokenException {
        try {
            return (float) value;
        } catch (ClassCastException e) {
            throw new Tokenizer.TokenException(line, pos, "Tried to get float from " + type);
        }
    }

    public enum Type implements TokenType {
        LBrack,
        RBrack,
        Equals,
        Slash,
        StrLit,
        IntLit,
        FloatLit,
        Identifier,
        EOF;

        @Override
        public boolean isEOF() {
            return this == EOF;
        }
    }
}
