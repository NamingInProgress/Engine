package com.vke.api.rendering.vulkan.descriptors.parsing;

import java.util.ArrayDeque;

public class HandleLexer {

    private final char[] source;
    private final ArrayDeque<HandleToken> putback = new ArrayDeque<>();

    private int index = 0;

    public HandleLexer(char[] source) {
        this.source = source;
    }

    public char pop() {
        if (index >= source.length) return '\0';
        return source[index++];
    }

    public char peek(int offset) {
        if (index + offset >= source.length) return '\0';
        return source[index + offset];
    }

    public char peek() {
        return peek(0);
    }

    public void putback(HandleToken token) {
        putback.addLast(token);
    }

    public HandleToken nextToken() {
        if (!putback.isEmpty()) return putback.removeLast();
        char c = pop();

        return switch (c) {
            case '\0' -> new HandleToken(TokenType.EOL);
            case '[' -> new HandleToken(TokenType.LBRACKET);
            case ']' -> new HandleToken(TokenType.RBRACKET);
            case '.' -> new HandleToken(TokenType.DOT);
            default -> {
                if (!isPartOfLiteral(c)) throw new IllegalStateException("Disallowed character found when parsing handle name! (" + c + ")");

                StringBuilder sb = new StringBuilder();
                sb.append(c);

                while (true) {
                    char nc = peek();
                    if (!isPartOfLiteral(nc)) break;
                    pop();
                    sb.append(nc);
                }

                try {
                    yield new HandleToken(TokenType.NUM_LITERAL, Integer.parseInt(sb.toString()));
                } catch (Exception _) {}

                yield new HandleToken(TokenType.LITERAL, sb.toString());
            }
        };
    }

    public HandleToken expectLiteral() {
        return expectLiteral("");
    }

    public HandleToken expectNumber() {
        return expectNumber("");
    }

    public HandleToken expectLBracket() {
        return expectLBracket("");
    }

    public HandleToken expectRBracket() {
        return expectRBracket("");
    }

    public HandleToken expectDot() {
        return expectDot("");
    }

    public HandleToken expectEOL() {
        return expectEOL("");
    }

    public HandleToken expectLiteral(String after) {
        HandleToken t = nextToken();
        if (t.type != TokenType.LITERAL) throw new IllegalStateException("Expected LITERAL" + (after.isEmpty() ? "" : " after " + after) + " but found " + t.type);
        return t;
    }

    public HandleToken expectNumber(String after) {
        HandleToken t = nextToken();
        if (t.type != TokenType.NUM_LITERAL) throw new IllegalStateException("Expected NUMBER" + (after.isEmpty() ? "" : " after " + after) + " but found " + t.type);
        return t;
    }

    public HandleToken expectLBracket(String after) {
        HandleToken t = nextToken();
        if (t.type != TokenType.LBRACKET) throw new IllegalStateException("Expected [" + (after.isEmpty() ? "" : " after " + after) + " but found " + t.type);
        return t;
    }

    public HandleToken expectRBracket(String after) {
        HandleToken t = nextToken();
        if (t.type != TokenType.RBRACKET) throw new IllegalStateException("Expected ]" + (after.isEmpty() ? "" : " after " + after) + " but found " + t.type);
        return t;
    }

    public HandleToken expectDot(String after) {
        HandleToken t = nextToken();
        if (t.type != TokenType.DOT) throw new IllegalStateException("Expected ." + (after.isEmpty() ? "" : " after " + after) + " but found " + t.type);
        return t;
    }

    public HandleToken expectEOL(String after) {
        HandleToken t = nextToken();
        if (t.type != TokenType.EOL) throw new IllegalStateException("Expected EOL" + (after.isEmpty() ? "" : " after " + after) + " but found " + t.type);
        return t;
    }

    public boolean isPartOfLiteral(char c) {
        return Character.isLetterOrDigit(c);
    }

    public static class HandleToken {

        public final TokenType type;
        public final Object value;

        public HandleToken(TokenType type) {
            this(type, null);
        }

        public HandleToken(TokenType type, Object value) {
            this.type = type;
            this.value = value;
        }

    }

    public enum TokenType {
        LITERAL,
        NUM_LITERAL,
        LBRACKET,
        RBRACKET,
        DOT,
        EOL
    }

}
