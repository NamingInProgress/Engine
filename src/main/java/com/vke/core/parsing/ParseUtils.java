package com.vke.core.parsing;

import com.vke.utils.fi.FaultySupplier;

import java.util.function.Supplier;

public class ParseUtils {
    public static <E extends Throwable> char escape(char c, FaultySupplier<Character, E> nextChar) throws E {
        if (c == '\\') {
            char escaped = nextChar.get();
            char literal = switch (escaped) {
                case '\\' -> '\\';
                case 't'  -> '\t';
                case 'r'  -> '\r';
                case '0'  -> '\0';
                case 'n'  -> '\n';
                case 'b'  -> '\b';
                case 'f'  -> '\f';
                case '\'' -> '\'';
                case '"'  -> '"';
                case 'u'  -> {
                    int value =
                            (hexof(nextChar.get()) << 12) |
                                    (hexof(nextChar.get()) <<  8) |
                                    (hexof(nextChar.get()) <<  4) |
                                    hexof(nextChar.get());
                    yield (char) value;
                }
                default -> 'L';
            };
            return literal;
        }
        return c;
    }

    public static int hexof(char c) {
        if (Character.isDigit(c)) {
            return c - '0';
        }
        char hex = Character.toUpperCase(c);
        return hex - 'A' + 10;
    }

    public static Object interpretString(String value) {
        if ("true".equals(value)) return true;
        if ("false".equals(value)) return false;

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ignore) {
            return value;
        }
    }
}
