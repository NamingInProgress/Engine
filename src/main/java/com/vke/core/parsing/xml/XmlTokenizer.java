package com.vke.core.parsing.xml;

import com.vke.api.parsing.SourceCode;
import com.vke.api.parsing.Tokenizer;

import java.util.LinkedList;
import java.util.Queue;

public class XmlTokenizer implements Tokenizer<XmlToken, XmlToken.Type> {
    private int line, pos;
    private Queue<XmlToken> putback;
    private SourceCode source;

    public XmlTokenizer(SourceCode source) {
        this.source = source;
        this.putback = new LinkedList<>();
    }

    @Override
    public XmlToken nextToken() {
        if (!putback.isEmpty()) {
            return putback.poll();
        }

        return null;
    }

    @Override
    public void putBack(XmlToken token) {
        putback.add(token);
    }

    @Override
    public int currentLine() {
        return line;
    }
}
