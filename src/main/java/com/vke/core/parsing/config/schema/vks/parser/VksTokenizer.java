package com.vke.core.parsing.config.schema.vks.parser;

import com.vke.core.parsing.ParseUtils;
import com.vke.core.parsing.SourceCursor;
import com.vke.core.parsing.config.schema.vks.parser.tkn.VksT;
import com.vke.core.parsing.config.schema.vks.parser.tkn.VksTT;

import java.io.IOException;

public class VksTokenizer {
    private final SourceCursor source;

    public VksTokenizer(SourceCursor source) {
        this.source = source;
    }

    public VksT nextToken() throws IOException {
        source.skipWhitespaces();

        char next = source.nextChar();
        return switch (next) {
            case '[' -> new VksT(VksTT.LBrack);
            case ']' -> new VksT(VksTT.RBrack);
            case '{' -> new VksT(VksTT.LBrace);
            case '}' -> new VksT(VksTT.RBrace);
            case ',' -> new VksT(VksTT.Comma);
            case ';' -> new VksT(VksTT.Semicolon);
            case '#' -> new VksT(VksTT.HashTag);
            case ':' -> new VksT(VksTT.Colon);
            case '!' -> new VksT(VksTT.Exclamation);
            case '|' -> new VksT(VksTT.Pipe);
            case '.' -> {
                char dot = source.nextChar();
                if (dot != '.') {
                    source.error_Unexpected('.');
                }
                yield new VksT(VksTT.DotDot);
            }
            case '"' -> {
                StringBuilder builder = new StringBuilder();
                while (true) {
                    char n = source.nextChar();
                    if (n == '"') {
                        break;
                    }
                    builder.append(ParseUtils.escape(n, source::nextChar));
                }
                yield new VksT(VksTT.QuStr, builder.toString());
            }

            default -> {
                if (!isStrOrNumPart(next)) {
                    source.error_Unexpected("A valid number or string");
                }

                boolean isNumber = true;
                StringBuilder builder = new StringBuilder();
                builder.append(next);
                while (true) {
                    char peek = source.peekChar();
                    if (!isStrOrNumPart(peek)) {
                        break;
                    }
                    if (!isNumPart(peek)) {
                        isNumber = false;
                    }
                    builder.append(peek);
                    source.nextChar();
                }

                String asStr = builder.toString();
                if (isNumber) {
                    try {
                        float number = Float.parseFloat(asStr);
                        yield new VksT(VksTT.Number, number);
                    } catch (NumberFormatException e) {
                        throw new IOException(e.getMessage());
                    }
                } else {
                    yield new VksT(VksTT.Str, asStr);
                }
            }
        };
    }

    private static boolean isStrOrNumPart(char c) {
        return Character.isLetterOrDigit(c) || "$._-".indexOf(c) > 0;
    }

    private static boolean isNumPart(char c) {
        return Character.isDigit(c) || "-.".indexOf(c) > 0;
    }

    public SourceCursor cursor() {
        return source;
    }
}
