package com.vke.test.parsing;

import com.vke.core.parsing.SourceCursor;

import java.io.IOException;

public class SourceCursorTest {
    public static void main(String[] args) throws IOException {
        String source = "let a = 5 && 6;";
        SourceCursor cursor = new SourceCursor(source.toCharArray(), 0);
        next(cursor);
        next(cursor);
        next(cursor);
        next(cursor);
        next(cursor);
        next(cursor);
        next(cursor);
        cursor.error_Unexpected(';', '+', '-');
    }

    private static void next(SourceCursor cursor) throws IOException {
        cursor.skipWhitespaces();
        cursor.nextChar();
    }
}
