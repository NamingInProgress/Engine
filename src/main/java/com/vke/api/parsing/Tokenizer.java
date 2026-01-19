package com.vke.api.parsing;

import com.vke.utils.Utils;

public interface Tokenizer<TK extends Token<TT>, TT extends TokenType> {
    TK nextToken() throws TokenizeException;
    void putBack(TK token);
    int currentLine();
    int currentPos();

    default TK expectSomeToken() throws TokenizeException {
        TK next = nextToken();
        if (next.getType().isEOF()) {
            throw TokenizeException.unexpectedToken(next);
        }
        return next;
    }

    default TK expectNext(TT... allowed) throws TokenizeException {
        TK next = nextToken();
        if (!Utils.TsContain(allowed, next.getType())) {
            throw TokenizeException.unexpectedToken(next, allowed);
        }
        return next;
    }

    class TokenizeException extends Exception {
        public TokenizeException(int line, int pos, String message) {
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

        public static <eTK extends Token<eTT>, eTT extends TokenType> TokenizeException unexpectedToken(eTK got, eTT... allowed) {
            String message = String.format("Unexpected Token! Expected %s, got %s!", arrOrAny(allowed), got.getType());
            return new TokenizeException(got.getLine(), got.getPosition(), message);
        }

        public static TokenizeException numberFormatException(int line, int pos, String number) {
            String message = String.format("Illegal number literal found: %s!", number);
            return new TokenizeException(line, pos, message);
        }
    }
}
