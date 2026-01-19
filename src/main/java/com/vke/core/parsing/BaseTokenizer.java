package com.vke.core.parsing;

import com.carrotsearch.hppc.CharArrayDeque;
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

    private final CharArrayDeque matchBuffer;

    public BaseTokenizer(SourceCode code) {
        this.putback = new LinkedList<>();
        this.code = new CharCursor(code);

        lineCommentMatcher = new CharSeqMatcher(supportsLineComments() ? lineCommentStart() : null);
        blockCommentMatcherStart = new CharSeqMatcher(supportsBlockComments() ? blockCommentStart() : null);
        blockCommentMatcherEnd = new CharSeqMatcher(supportsBlockComments() ? blockCommentEnd() : null);
        stringMatcherStart = new CharSeqMatcher(supportsStrings() ? stringStart() : null);
        stringMatcherEnd = new CharSeqMatcher(supportsStrings() ? stringEnd() : null);

        matchBuffer = new CharArrayDeque();
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
        if (!matchBuffer.isEmpty()) {
            return matchBuffer.removeFirst();
        }
        return code.next();
    }

    private char peek() {
        if (!matchBuffer.isEmpty()) {
            return matchBuffer.getFirst();
        }
        return code.peek();
    }

    @Override
    public TK nextToken() {
        if (!putback.isEmpty()) {
            return putback.poll();
        }

        char next = next();

        if (handleLineComments(next)) {
            next = next();
        }




        return null;
    }

    private boolean handleLineComments(char next) {
        char c = next;
        while (lineCommentMatcher.tryMatch(c)) {
            matchBuffer.addLast(c);
            c = next();
        }
        if (lineCommentMatcher.foundMatch()) {
            consumeUntilNewline();
            matchBuffer.clear();
        }
        lineCommentMatcher.reset();
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

    protected class CharCursor {
        private final SourceCode code;
        private int line = 1, column = 1;

        protected CharCursor(SourceCode code) {
            this.code = code;
        }

        char next() {
            char c = code.next();
            if (c == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            return c;
        }

        char peek() {
            return code.peek();
        }

        int line() { return line; }
        int column() { return column; }
    }

}
