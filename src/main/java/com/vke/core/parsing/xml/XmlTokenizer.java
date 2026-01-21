package com.vke.core.parsing.xml;

import com.vke.api.parsing.SourceCode;
import com.vke.core.parsing.BaseTokenizer;

public class XmlTokenizer extends BaseTokenizer<XmlToken, XmlToken.Type> {
    public XmlTokenizer(SourceCode code) {
        super(code);
    }

    @Override
    protected boolean supportsLineComments() {
        return false;
    }

    @Override
    protected CharSequence lineCommentStart() {
        return null;
    }

    @Override
    protected boolean supportsBlockComments() {
        return true;
    }

    @Override
    protected CharSequence blockCommentStart() {
        return "<!--";
    }

    @Override
    protected CharSequence blockCommentEnd() {
        return "-->";
    }

    @Override
    protected boolean supportsStrings() {
        //we will be using identifiers!
        return false;
    }

    @Override
    protected CharSequence stringStart() {
        return null;
    }

    @Override
    protected CharSequence stringEnd() {
        return null;
    }

    @Override
    protected XmlToken createStringToken(String string) {
        return null;
    }

    @Override
    protected boolean supportsNumbers() {
        return true;
    }

    @Override
    protected XmlToken createIntToken(int i) {
        return new XmlToken(currentLine(), currentPos(), XmlToken.Type.IntLit, i);
    }

    @Override
    protected XmlToken createFloatToken(float f) {
        return new XmlToken(currentLine(), currentPos(), XmlToken.Type.FloatLit, f);
    }

    @Override
    protected XmlToken matchSimpleToken(BaseTokenizer.CharCursor c) {
        char next = c.next();
        switch (next) {
            case '<' -> new XmlToken(c.line(), c.pos(), XmlToken.Type.LBrack);
            case '>' -> new XmlToken(c.line(), c.pos(), XmlToken.Type.RBrack);
            case '=' -> new XmlToken(c.line(), c.pos(), XmlToken.Type.Equals);
            case '/' -> new XmlToken(c.line(), c.pos(), XmlToken.Type.Slash);
            default -> {
                c.putBack(next);
                return null;
            }
        }
        //how do i need that
        return null;
    }

    @Override
    protected XmlToken createIdentifierToken(String ident) {
        return new XmlToken(currentLine(), currentPos(), XmlToken.Type.Identifier, ident);
    }

    @Override
    protected boolean validForIdentifier(char c) {
        return c != '"' && c != '<';
    }

    @Override
    protected int supportedEscapePoints() {
        return BaseTokenizer.ESCAPE_IDENTIFIERS | BaseTokenizer.ESCAPE_STRINGS;
    }

    @Override
    protected char escapeChar() {
        return '\\';
    }
}
