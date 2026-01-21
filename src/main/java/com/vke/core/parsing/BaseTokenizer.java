package com.vke.core.parsing;

import com.carrotsearch.hppc.CharArrayDeque;
import com.carrotsearch.hppc.procedures.CharProcedure;
import com.vke.api.parsing.SourceCode;
import com.vke.api.parsing.Token;
import com.vke.api.parsing.TokenType;
import com.vke.api.parsing.Tokenizer;

import java.util.LinkedList;
import java.util.Queue;

public abstract class BaseTokenizer<TK extends Token<TT>, TT extends TokenType> implements Tokenizer<TK, TT> {
    public static final int ESCAPE_NONE = 0;
    public static final int ESCAPE_STRINGS = 1;
    public static final int ESCAPE_IDENTIFIERS = 2;

    private final Queue<TK> putback;
    private CharCursor cursor;

    private final CharSeqMatcher lineCommentMatcher;
    private final CharSeqMatcher blockCommentMatcherStart;
    private final CharSeqMatcher blockCommentMatcherEnd;
    private final CharSeqMatcher stringMatcherStart;
    private final CharSeqMatcher stringMatcherEnd;

    public BaseTokenizer(SourceCode code) {
        this.putback = new LinkedList<>();
        this.cursor = new CharCursor(code);

        lineCommentMatcher = new CharSeqMatcher(supportsLineComments() ? lineCommentStart() : null);
        blockCommentMatcherStart = new CharSeqMatcher(supportsBlockComments() ? blockCommentStart() : null);
        blockCommentMatcherEnd = new CharSeqMatcher(supportsBlockComments() ? blockCommentEnd() : null);
        stringMatcherStart = new CharSeqMatcher(supportsStrings() ? stringStart() : null);
        stringMatcherEnd = new CharSeqMatcher(supportsStrings() ? stringEnd() : null);
    }

    protected abstract boolean supportsLineComments();
    protected abstract CharSequence lineCommentStart();
    protected abstract boolean supportsBlockComments();
    protected abstract CharSequence blockCommentStart();
    protected abstract CharSequence blockCommentEnd();
    protected abstract boolean supportsStrings();
    protected abstract CharSequence stringStart();
    protected abstract CharSequence stringEnd();
    protected abstract TK createStringToken(String string);
    protected abstract boolean supportsNumbers();
    protected abstract TK createIntToken(int i);
    protected abstract TK createFloatToken(float f);
    protected abstract TK matchSimpleToken(CharCursor c);
    protected abstract TK createIdentifierToken(String ident);
    protected abstract boolean validForIdentifier(char c);
    protected abstract int supportedEscapePoints();
    protected abstract char escapeChar();
    protected abstract TK createEOFToken();

    private boolean supportsStringEscape() {
        return (supportedEscapePoints() & ESCAPE_STRINGS) == ESCAPE_STRINGS;
    }

    private boolean supportsIdentEscape() {
        return (supportedEscapePoints() & ESCAPE_IDENTIFIERS) == ESCAPE_IDENTIFIERS;
    }

    private char next() {
        return cursor.next();
    }

    @Override
    public TK nextToken() throws TokenizeException {
        if (!putback.isEmpty()) {
            return putback.poll();
        }

        if (!cursor.hasNext()) {
            return createEOFToken();
        }

        char next = next();

        if (handleSkips(next)) {
            next = next();
        }
        cursor.putBack(next);

        TK mightBeToken = matchSimpleToken(cursor);
        if (mightBeToken != null) {
            return mightBeToken;
        }

        next = next();

        //check numbers if supported and collect
        if (supportsNumbers()) {
            if (isNumber(next)) {
                StringBuilder numberBuilder = new StringBuilder();
                boolean isFloat = false;
                do {
                    if (next == '.') {
                        isFloat = true;
                    }
                    numberBuilder.append(next);
                    next = next();
                } while (isNumberPart(next));
                String numberString = numberBuilder.toString();

                try {
                    if (isFloat) {
                        float f = Float.parseFloat(numberString);
                        return createFloatToken(f);
                    } else {
                        int i = Integer.parseInt(numberString);
                        return createIntToken(i);
                    }
                } catch (NumberFormatException e) {
                    throw TokenizeException.numberFormatException(currentLine(), cursor.pos(), numberString);
                }
            }
        }

        //check if strings are supported and collect
        if (supportsStrings()) {
            if (matches(next, stringMatcherStart, 0)) {
                //lol this one actually builds strings for once
                StringBuilder stringBuilder = new StringBuilder();
                do {
                    stringBuilder.append(next);
                    next = next();
                } while (!matches(next, stringMatcherEnd, ESCAPE_STRINGS));
                return createStringToken(stringBuilder.toString());
            }
        }

        StringBuilder identBuilder = new StringBuilder();
        do {
            identBuilder.append(next);
            next = next();
            if (supportsIdentEscape()) {
                if (next == escapeChar()) {
                    char escaped = next();
                    identBuilder.append(escaped);
                    next = next();
                }
            }
        } while (validForIdentifier(next));

        return createIdentifierToken(identBuilder.toString());
    }

    private boolean isNumber(char c) {
        //4
        //-2
        return Character.isDigit(c) || c == '-';
    }

    private boolean isNumberPart(char c) {
        return Character.isDigit(c) || c == '_' || c == '.';
    }

    private boolean handleSkips(char next) {
        boolean found = false;
        if (supportsLineComments() && handleLineComment(next)) {
            found = true;
            next = next();
        }
        if (supportsBlockComments() && handleBlockComment(next)) {
            found = true;
            next = next();
        }
        if (found) {
            //there might be even more skips
            handleSkips(next);
            return true;
        }
        return false;
    }

    private boolean handleLineComment(char next) {
        if (matches(next, lineCommentMatcher, 0)) {
            consumeUntilNewline();
            return true;
        }
        return false;
    }

    private boolean handleBlockComment(char next) {
        if (matches(next, blockCommentMatcherStart, 0)) {
            char innerNext = next();
            while (!matches(innerNext, blockCommentMatcherEnd, 0)) {
                innerNext = next();
            }

            return true;
        }
        return false;
    }

    private boolean matches(char next, CharSeqMatcher matcher, int escapePoint) {
        char c = next;
        CharArrayDeque tmp = new CharArrayDeque();
        while (matcher.tryMatch(c)) {
            tmp.addLast(c);
            c = next();
            if (
                    (escapePoint == ESCAPE_STRINGS && supportsStringEscape()) ||
                    (escapePoint == ESCAPE_IDENTIFIERS && supportsIdentEscape())) {

                while (c == escapeChar()) {
                    char toEscape = next();
                    tmp.addLast(toEscape);
                    c = next();
                }
            }
        }
        if (matcher.foundMatch()) {
            matcher.reset();
            cursor.putBack(c);
            return true;
        }
        tmp.forEach((CharProcedure) cursor::putBack);
        cursor.putBack(c);
        matcher.reset();
        return false;
    }

    private void consumeUntilNewline() {
        char next = cursor.next();
        while (next != '\n' && next != '\r') {
            next = cursor.next();
        }
        char lf = cursor.next();
        if (lf != '\n') {
            cursor.putBack(lf);
        }
    }

    @Override
    public void putBack(TK token) {
        putback.add(token);
    }

    @Override
    public int currentLine() {
        return cursor.line();
    }

    @Override
    public int currentPos() {
        return cursor.pos();
    }

    private static class CharSeqMatcher {
        private final CharSequence seq;
        private int index;

        private CharSeqMatcher(CharSequence seq) {
            this.seq = seq;
        }

        boolean tryMatch(char c) {
            if (seq == null) return false;
            if (index >= seq.length()) return false;
            if (c == seq.charAt(index)) {
                index++;
                return true;
            }
            return false;
        }

        void reset() {
            index = 0;
        }

        boolean foundMatch() {
            return index >= seq.length();
        }
    }

    public static class CharCursor {
        private final CharArrayDeque putback;
        private final SourceCode code;
        private int line = 1, column = 1;

        protected CharCursor(SourceCode code) {
            this.code = code;
            this.putback = new CharArrayDeque();
        }

        private char next0() {
            if (!putback.isEmpty()) {
                return putback.removeFirst();
            }
            return code.next();
        }

        public char next() {
            char c = next0();
            if (c == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            return c;
        }

        public boolean hasNext() {
            return !putback.isEmpty() || code.hasNext();
        }

        public int line() { return line; }
        public int pos() { return column; }

        public void putBack(char c) {
            putback.addLast(c);
        }
    }

}
