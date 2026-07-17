package com.vke.core.parsing;

import java.io.IOException;
import java.util.Arrays;

public class SourceCursor {
    private static final int ERROR_HISTORY_SIZE = 30;

    private final char[] source;
    private State st;
    private State marker;
    private String filename;

    private String errorMessage;

    public SourceCursor(char[] source, int index) {
        this.source = source;
        this.st = new State();
        st.index = index;
    }

    public SourceCursor(char[] source, int index, String filename) {
        this.source = source;
        this.st = new State();
        st.index = index;
        this.filename = filename;
    }

    public char nextChar() throws EOF {
        if (st.index >= source.length) throw new EOF();
        st.lastChar = source[st.index++];
        st.onChar();
        return st.lastChar;
    }

    public char peekChar() {
        return peekChar(0);
    }

    public char peekChar(int dist) {
        if (st.index + dist >= source.length) return '\0';
        return source[st.index + dist];
    }

    public void skipWhitespaces() throws EOF {
        while (true) {
            char p = peekChar();
            if (Character.isWhitespace(p)) {
                nextChar();
            } else {
                break;
            }
        }
    }

    public void mark() {
        this.marker = new State();
        marker.index = st.index;
        marker.col = st.col;
        marker.row = st.row;
        marker.history = st.history;
        marker.historyIndex = st.historyIndex;
        marker.historyFull = st.historyFull;
    }

    public void reset() {
        this.st = marker;
        if (st == null) {
            st = new State();
        }
    }

    public void error_Unexpected(char... expected) throws IOException {
        errorMessage = String.format("Found '%s', expected: %s", st.lastChar, Arrays.toString(expected));
        buildError();
    }

    public void error_Unexpected(String expected) throws IOException {
        errorMessage = String.format("Found '%s', expected: %s", st.lastChar, expected);
        buildError();
    }

    private void buildError() throws IOException {
        String main;
        if (filename != null) {
            main = String.format("%s [%d:%d] Error: %s", filename, st.row, st.col, errorMessage);
        } else {
            main = String.format("[%d:%d] Error: %s", st.row, st.col, errorMessage);
        }
        String history = st.buildHistory();
        String message;
        if (st.index >= ERROR_HISTORY_SIZE) {
            message = String.format("%s%n'...%s' <<< HERE", main, history);
        } else {
            message = String.format("%s%n'%s' <<< HERE", main, history);
        }
        throw new IOException(message);
    }

    public static class EOF extends IOException {}

    private static class State {
        private int index;
        private int col, row;
        private char lastChar;
        private char[] history;
        private int historyIndex;
        private boolean historyFull;

        private State() {
            this.history = new char[ERROR_HISTORY_SIZE];
            this.col = 0;
            this.row = 1;
        }

        private void onChar() {
            if (lastChar == '\n') {
                row++;
                col = 1;
            } else {
                col++;
            }

            history[historyIndex] = lastChar;
            historyIndex++;
            if (historyIndex >= ERROR_HISTORY_SIZE) {
                historyIndex = 0;
                historyFull = true;
            }
        }

        private String buildHistory() {
            if (historyFull) {
                StringBuilder builder = new StringBuilder(ERROR_HISTORY_SIZE);
                for (int i = 0; i < ERROR_HISTORY_SIZE; i++) {
                    builder.append(history[(i + historyIndex) % ERROR_HISTORY_SIZE]);
                }
                return builder.toString();
            } else {
                int length = historyIndex == 0 ? ERROR_HISTORY_SIZE : historyIndex;
                StringBuilder builder = new StringBuilder(length);
                for (int i = 0; i < length; i++) {
                    builder.append(history[i]);
                }
                return builder.toString();
            }
        }
    }
}
