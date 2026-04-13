package com.vke.core.parsing.config.xml.tokens;

import com.vke.core.parsing.SourceCursor;

import java.util.ArrayDeque;

public class XmlTokenizer {
    private final SourceCursor source;
    private final ArrayDeque<XmlToken> putback;

    private boolean inTagHead;

    public XmlTokenizer(SourceCursor source) {
        this.source = source;
        this.putback = new ArrayDeque<>();
    }

    public void putback(XmlToken token) {
        putback.addLast(token);
    }

    public void setInTagHead(boolean inTagHead) {
        this.inTagHead = inTagHead;
    }

    public String collectContent() {
        StringBuilder builder = new StringBuilder();
        while (source.peekChar() != '<') {
            try {
                builder.append(source.nextChar());
            } catch (SourceCursor.EOF e) {
                break;
            }
        }
        return builder.toString();
    }

    public void resetToPreviousPosition() {
        source.reset();
    }

    public XmlToken expectToken(XmlToken.Type type) throws IllegalStateException, SourceCursor.EOF, NumberFormatException {
        XmlToken next = nextToken();
        if (next.getType() != type) {
            throw new IllegalStateException("Unexpected Token! Expected " + type + ", got " + next.getType());
        }
        return next;
    }

    public XmlToken nextToken() throws SourceCursor.EOF, IllegalStateException, NumberFormatException {
        if (!putback.isEmpty()) return putback.removeFirst();

        source.mark();
        source.skipWhitespaces();
        char next = source.nextChar();
        if (next == '<') {
            return new XmlToken(XmlToken.Type.LTri);
        } else {
            if (inTagHead) {
                if (next == '>') return new XmlToken(XmlToken.Type.RTri);
                if (next == '?') return new XmlToken(XmlToken.Type.Question);
                if (next == '/') return new XmlToken(XmlToken.Type.Slash);
                if (next == '=') return new XmlToken(XmlToken.Type.Eq);
                if (next == '!') return new XmlToken(XmlToken.Type.Exclamation);
                if (next == '-') return new XmlToken(XmlToken.Type.Dash);

                if (next == '"') {
                    //attrib with ""
                    StringBuilder builder = new StringBuilder();
                    while (source.peekChar() != '"') {
                        builder.append(source.nextChar());
                    }
                    source.nextChar();
                    return new XmlToken(XmlToken.Type.StrLit, builder.toString());
                }
                if (next == '\'') {
                    //attrib with ''
                    StringBuilder builder = new StringBuilder();
                    while (source.peekChar() != '\'') {
                        builder.append(source.nextChar());
                    }
                    source.nextChar();
                    return new XmlToken(XmlToken.Type.StrLit, builder.toString());
                }

                StringBuilder identBuilder = new StringBuilder();
                identBuilder.append(next);
                while (identPart(source.peekChar())) {
                    identBuilder.append(source.nextChar());
                }
                return new XmlToken(XmlToken.Type.Ident, identBuilder.toString());
            }
            return new XmlToken(XmlToken.Type.Illegal);
        }
    }

    private boolean identPart(char c) {
        return Character.isLetterOrDigit(c) || "_-".indexOf(c) > 0;
    }
}
