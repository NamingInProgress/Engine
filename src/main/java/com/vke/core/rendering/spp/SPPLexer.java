package com.vke.core.rendering.spp;

import java.util.ArrayDeque;

public class SPPLexer {

    private final char[] source;
    private final ArrayDeque<SPPToken> putback = new ArrayDeque<>();

    private int index = 0;

    public SPPLexer(char[] source) {
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

    public void putback(SPPToken token) {
        putback.addLast(token);
    }

    public SPPToken nextToken() {
        if (!putback.isEmpty()) return putback.removeLast();
        while (true) {
            while (Character.isWhitespace(peek())) {
                pop();
            }

            if (peek() == '/' && peek(1) == '/') {
                while (peek() != '\n' && peek() != '\0') {
                    pop();
                }
                continue;
            }

            if (peek() == '/' && peek(1) == '*') {
                pop(); // /
                pop(); // *

                while (!(peek() == '*' && peek(1) == '/')) {
                    if (peek() == '\0') {
                        throw new RuntimeException("Unterminated block comment");
                    }
                    pop();
                }

                pop(); // *
                pop(); // /

                continue;
            }

            break;
        }
        char c = pop();

        return switch (c) {
            case '\0' -> new SPPToken(TokenType.EOF);
            case '#' -> new SPPToken(TokenType.HASHTAG);
            case '[' -> new SPPToken(TokenType.LBRACKET);
            case ']' -> new SPPToken(TokenType.RBRACKET);
            case '(' -> new SPPToken(TokenType.LPAREN);
            case ')' -> new SPPToken(TokenType.RPAREN);
            case '{' -> new SPPToken(TokenType.LBRACE);
            case '}' -> new SPPToken(TokenType.RBRACE);
            default -> {
                StringBuilder sb = new StringBuilder();
                sb.append(c);
                int startIndex = index - 1;

                while (true) {
                    char nc = peek();
                    if (!isPartOfLiteral(nc)) break;
                    pop();
                    sb.append(nc);
                }

                try {
                    yield new SPPToken(TokenType.NUM_LITERAL, Integer.parseInt(sb.toString()), startIndex, index);
                } catch (Exception _) {}

                yield new SPPToken(TokenType.LITERAL, sb.toString(), startIndex, index);
            }
        };
    }

    public boolean isPartOfLiteral(char c) {
        return Character.isLetterOrDigit(c) || c == '/' || c == '.' || c == '_' || c == '"' || c == ':';
    }

    public class SPPToken {

        public final TokenType type;
        public final Object value;
        public final int start, end;

        public SPPToken(TokenType type) {
            this(type, null);
        }

        public SPPToken(TokenType type, Object value) {
            this(type,value, SPPLexer.this.index - 1, SPPLexer.this.index);
        }

        public SPPToken(TokenType type, Object value, int start, int end) {
            this.type = type;
            this.value = value;
            this.start = start;
            this.end = end;
        }

    }

    public enum TokenType {
        LITERAL,
        NUM_LITERAL,
        LBRACKET,
        RBRACKET,
        LPAREN,
        RPAREN,
        LBRACE,
        RBRACE,
        HASHTAG,
        EOF
    }
    
}
