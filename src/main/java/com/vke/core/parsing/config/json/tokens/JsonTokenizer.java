package com.vke.core.parsing.config.json.tokens;

import com.vke.core.parsing.ParseUtils;
import com.vke.core.parsing.SourceCursor;

import java.util.ArrayDeque;

public class JsonTokenizer {
    private final SourceCursor source;
    private final ArrayDeque<JsonToken> putback;

    public JsonTokenizer(SourceCursor source) {
        this.source = source;
        this.putback = new ArrayDeque<>();
    }

    public void putback(JsonToken token) {
        putback.addLast(token);
    }

    public JsonToken expectToken(JsonToken.Type type) throws IllegalStateException, SourceCursor.EOF, NumberFormatException {
        JsonToken next = nextToken();
        if (next.getType() != type) {
            throw new IllegalStateException("Unexpected Token! Expected " + type + ", got " + next.getType());
        }
        return next;
    }

    public JsonToken nextToken() throws SourceCursor.EOF, NumberFormatException {
        if (!putback.isEmpty()) return putback.removeFirst();

        source.skipWhitespaces();
        char next = source.nextChar();
        return switch (next) {
            case '{' -> new JsonToken(JsonToken.Type.LBrace);
            case '}' -> new JsonToken(JsonToken.Type.RBrace);
            case '[' -> new JsonToken(JsonToken.Type.LBrack);
            case ']' -> new JsonToken(JsonToken.Type.RBrack);
            case ':' -> new JsonToken(JsonToken.Type.Colon);
            case ',' -> new JsonToken(JsonToken.Type.Comma);
            default -> {
                if (next == '"') {
                    StringBuilder builder = new StringBuilder();
                    while (true) {
                        char n = source.nextChar();
                        if (n == '"') {
                            break;
                        }
                        builder.append(ParseUtils.escape(n, source::nextChar));
                    }
                    yield new JsonToken(JsonToken.Type.StrLit, builder.toString());
                } else {
                    StringBuilder numBuilder = new StringBuilder();
                    numBuilder.append(next);
                    while (true) {
                        char n = source.peekChar();
                        if (numPart(n)) {
                            source.nextChar();
                            numBuilder.append(n);
                        } else {
                            break;
                        }
                    }
                    Float f = Float.parseFloat(numBuilder.toString());
                    yield new JsonToken(JsonToken.Type.NumLit, f);
                }
            }
        };
    }

    private boolean numPart(char c) {
        return Character.isDigit(c) || "e.".indexOf(c) >= 0;
    }
}
