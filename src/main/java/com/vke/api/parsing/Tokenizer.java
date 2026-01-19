package com.vke.api.parsing;

import com.vke.utils.Utils;

public interface Tokenizer<TK extends Token<TT>, TT extends TokenType> {
    TK nextToken();
    void putBack(TK token);
    int currentLine();

    default TK expectSomeToken() throws TokenException {
        TK next = nextToken();
        if (next.getType().isEOF()) {
            throw TokenException.unexpectedToken(next);
        }
        return next;
    }

    default TK expectNext(TT... allowed) throws TokenException {
        TK next = nextToken();
        if (!Utils.TsContain(allowed, next.getType())) {
            throw TokenException.unexpectedToken(next, allowed);
        }
        return next;
    }

    class TokenException extends Exception {
        public TokenException(int line, int pos, String message) {
            super(String.format("[%d:%d] %s", line, pos, message));
        }

        private static <T> String arrOrAny(T... arr) {
            StringBuilder builder = new StringBuilder();
            if (arr.length > 0) {
                if (arr.length > 1) {
                    builder.append('[');
                    for (int i = 0; i < arr.length; i++) {
                        builder.append(arr[i]);
                        if (i != arr.length - 1) {
                            builder.append(", ");
                        }
                    }
                    builder.append(']');
                }
            } else {
                builder.append("<any>");
            }
            return builder.toString();
        }

        public static <eTK extends Token<eTT>, eTT extends TokenType> TokenException unexpectedToken(eTK got, eTT... allowed) {
            String message = String.format("Unexpected Token! Expected %s, got %s!", arrOrAny(allowed), got.getType());
            return new TokenException(got.getLine(), got.getPosition(), message);
        }
    }
}
