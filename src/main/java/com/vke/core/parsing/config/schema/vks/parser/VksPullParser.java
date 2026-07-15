package com.vke.core.parsing.config.schema.vks.parser;

import com.vke.core.parsing.SourceCursor;
import com.vke.core.parsing.config.schema.vks.parser.tkn.VksT;
import com.vke.core.parsing.config.schema.vks.parser.tkn.VksTT;
import com.vke.core.parsing.config.schema.vks.parser.tkn.VksTokens;
import com.vke.core.parsing.config.schema.vks.parser.type.*;
import com.vke.utils.Utils;
import com.vke.utils.exception.Unreachable;
import com.vke.utils.functionalinterface.FaultyFunction;
import com.vke.utils.io.Identifier;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VksPullParser {
    private final VksTokens tokens;

    public VksPullParser(Identifier identifier) throws IOException {
        char[] source = Utils.readCharsFromInputStream(identifier.asInputStream());
        SourceCursor cursor = new SourceCursor(source, 0, identifier.toString());
        VksTokenizer tokenizer = new VksTokenizer(cursor);
        this.tokens = new VksTokens(tokenizer);
    }

    public VksPullParser(char[] source) throws IOException {
        SourceCursor cursor = new SourceCursor(source, 0, "<some vks file>");
        VksTokenizer tokenizer = new VksTokenizer(cursor);
        this.tokens = new VksTokens(tokenizer);
    }

    public <T> List<T> parseAllArgs(FaultyFunction<VksPullParser, T, IOException> parser) throws IOException {
        VksT next = peek();
        if (next == null || next.getType() != VksTT.LBrack) {
            return List.of();
        }
        next();

        VksT tmp;
        ArrayList<T> parsed = new ArrayList<>();
        do {
            parsed.add(parser.apply(this));
        } while((tmp = peek()) != null && tmp.getType() == VksTT.Comma && next() == tmp);
        VksT hopefullyTheClosingBrack = next();
        if (hopefullyTheClosingBrack != null && hopefullyTheClosingBrack.getType() == VksTT.RBrack) {
            return parsed;
        }
        throw new IOException("Expected ], found: " + hopefullyTheClosingBrack.getType());
    }

    public <T> T parseNextArg(FaultyFunction<VksPullParser, T, IOException> parser) throws IOException {
        VksT next = peek();
        if (next == null) {
            throw new EOFException();
        }
        if (next.getType() == VksTT.LBrack || next.getType() == VksTT.Comma) {
            next();
        }
        T t = parser.apply(this);
        next = peek();
        if (next != null && next.getType() == VksTT.RBrack) {
            next();
        }
        return t;
    }

    public <T> T parseThing(FaultyFunction<VksPullParser, T, IOException> parser) throws IOException {
        return parser.apply(this);
    }

    public boolean peek(VksTT vksTT) throws IOException {
        VksT t;
        return (t = tokens.peek()) != null && t.getType() == vksTT;
    }

    public VksT peek() throws IOException {
        return tokens.peek();
    }

    public VksT next() throws IOException {
        return tokens.next();
    }

    public VksT expect(VksTT vksTT) throws IOException {
        VksT next = peek();
        if (next != null && next.getType() == vksTT) {
            return next();
        }
        tokens.cursor().error_Unexpected(vksTT.name());
        throw new Unreachable();
    }

    public static String parseString(VksPullParser parser) throws IOException {
        VksT next = parser.peek();
        if (next != null && next.getType() == VksTT.Str) {
            parser.next();
            return next.getDataAs();
        }
        parser.tokens.cursor().error_Unexpected("String");
        throw new Unreachable();
    }

    public static String parseQuotString(VksPullParser parser) throws IOException {
        VksT next = parser.peek();
        if (next != null && next.getType() == VksTT.QuStr) {
            parser.next();
            return next.getDataAs();
        }
        parser.tokens.cursor().error_Unexpected("QuotString");
        throw new Unreachable();
    }

    public static float parseNumber(VksPullParser parser) throws IOException {
        VksT next = parser.peek();
        if (next != null && next.getType() == VksTT.Number) {
            parser.next();
            return next.getDataAs();
        }
        parser.tokens.cursor().error_Unexpected("Number");
        throw new Unreachable();
    }

    public static VksTypeDeclaration parseTypeDeclaration(VksPullParser parser) throws IOException {
        List<VksTypeDeclaration> types = new ArrayList<>();
        do {
            VksTypeDeclaration base;
            if (parser.peek(VksTT.Str)) {
                String typeName = parseString(parser);
                if (typeName == null) {
                    return null;
                }
                base = switch (typeName) {
                    case "string" -> new StringTypeDeclaration(parser);
                    case "number" -> new NumberTypeDeclaration(parser);
                    case "boolean" -> new BooleanTypeDeclaration();
                    default -> new TypeReferenceDeclaration(typeName);
                };
            } else {
                if (parser.peek(VksTT.LBrack)) {
                    parser.next();
                    VksTypeDeclaration inner = parseTypeDeclaration(parser);
                    parser.expect(VksTT.RBrack);
                    base = new ArrayTypeDeclaration(inner);
                }else if (parser.peek(VksTT.LBrace)) {
                    base = new ObjectTypeDeclaration(parser);
                } else {
                    parser.tokens.cursor().error_Unexpected("Valid type declaration");
                    throw new Unreachable();
                }
            }
            types.add(base);
        } while (parser.peek(VksTT.Pipe) && parser.next() != null);

        if (types.size() == 1) {
            return types.getFirst();
        }
        return new MultiTypeDeclaration(types);
    }
}
