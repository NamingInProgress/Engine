package com.vke.api.parsing;

public interface Token<TT extends TokenType> {
    int getLine();
    int getPosition();
    TT getType();
}
