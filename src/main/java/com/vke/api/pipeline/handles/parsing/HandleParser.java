package com.vke.api.pipeline.handles.parsing;

import com.vke.api.pipeline.handles.parsing.node.*;

public class HandleParser {

    public BaseNode parse(String line) {
        HandleLexer lexer = new HandleLexer(line.toCharArray());
        HandleLexer.HandleToken token = lexer.nextToken();
        BaseNode master = new BaseNode();

        if (token.type == HandleLexer.TokenType.LITERAL) {
            master.child = parseFirst(lexer, token);
        } else {
            throw new IllegalStateException("Uniform Handle name must start with a literal!");
        }

        return master;
    }

    public BindingNode parseFirst(HandleLexer lexer, HandleLexer.HandleToken currentToken) {
        BindingNode n = new BindingNode((String) currentToken.value);
        Node currentNode = n;

        outer:
        while (true) {
            HandleLexer.HandleToken next = lexer.nextToken();

            switch (next.type) {
                case DOT -> {
                    currentNode.child = parseLayer(lexer);
                    currentNode = currentNode.child;
                }
                case LBRACKET -> {
                    currentNode.child = parseArray(lexer);
                    currentNode = currentNode.child;
                }
                case EOL -> { break outer; }
                default -> throw new IllegalStateException("Disallowed token (" + next.type + ") found when parsing Uniform Handle name!");
            }
        }

        return n;
    }

    public ArrayIndexNode parseArray(HandleLexer lexer) {
        HandleLexer.HandleToken numberHopefully = lexer.expectNumber("[");
        lexer.expectRBracket("NUMBER");

        return new ArrayIndexNode((int) numberHopefully.value);
    }

    public EntryNode parseLayer(HandleLexer lexer) {
        HandleLexer.HandleToken literalHopefully = lexer.expectLiteral(".");
        return new EntryNode((String) literalHopefully.value);
    }

}
