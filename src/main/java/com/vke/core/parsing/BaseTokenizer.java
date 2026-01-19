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
    private final Queue<TK> putback;
    private CharCursor code;

    private final CharSeqMatcher lineCommentMatcher;
    private final CharSeqMatcher blockCommentMatcherStart;
    private final CharSeqMatcher blockCommentMatcherEnd;
    private final CharSeqMatcher stringMatcherStart;
    private final CharSeqMatcher stringMatcherEnd;

    public BaseTokenizer(SourceCode code) {
        this.putback = new LinkedList<>();
        this.code = new CharCursor(code);

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
    protected abstract TK createIntToken(int string);
    protected abstract TK createFloatToken(float string);
    protected abstract TK matchSimpleToken(CharCursor c);

    private char next() {
        return code.next();
    }

    private char peek() {
        return code.peek();
    }

    @Override
    public TK nextToken() {
        if (!putback.isEmpty()) {
            return putback.poll();
        }

        char next = next();

        if (handleSkips(next)) {
            next = next();
        }
        code.putBack(next);

        TK mightBeToken = matchSimpleToken(code);
        if (mightBeToken != null) {
            return mightBeToken;
        }

        next = next();

        //check numbers if supported and collect
        if (supportsNumbers()) {
            if (mightBeNumber(next)) {
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
                //TODO: add check for number syntax error
                if (isFloat) {
                    float f = Float.parseFloat(numberString);
                    return createFloatToken(f);
                } else {
                    int i = Integer.parseInt(numberString);
                    return createIntToken(i);
                }
            }
        }

        return null;
    }

    private boolean mightBeNumber(char c) {
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
        if (matches(next, lineCommentMatcher)) {
            consumeUntilNewline();
            return true;
        }
        return false;
    }

    private boolean handleBlockComment(char next) {
        if (matches(next, blockCommentMatcherStart)) {
            char innerNext = next();
            while (!matches(innerNext, blockCommentMatcherEnd)) {
                innerNext = next();
            }

            return true;
        }
        return false;
    }

    private boolean matches(char next, CharSeqMatcher matcher) {
        char c = next;
        CharArrayDeque tmp = new CharArrayDeque();
        while (matcher.tryMatch(c)) {
            tmp.addLast(c);
            c = next();
        }
        if (matcher.foundMatch()) {
            matcher.reset();
            return true;
        }
        tmp.forEach((CharProcedure) code::putBack);
        matcher.reset();
        return false;
    }

    private void consumeUntilNewline() {
        char next = code.next();
        while (next != '\n' && next != '\r') {
            next = code.next();
        }
        char lf = code.peek();
        if (lf == '\n') {
            code.next();
        }
    }

    @Override
    public void putBack(TK token) {
        putback.add(token);
    }

    @Override
    public int currentLine() {
        return code.line();
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

    protected static class CharCursor {
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

        char next() {
            char c = next0();
            if (c == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            return c;
        }

        char peek() {
            if (!putback.isEmpty()) {
                return putback.getFirst();
            }
            return code.peek();
        }

        int line() { return line; }
        int column() { return column; }

        void putBack(char c) {
            putback.addLast(c);
        }
    }

}
