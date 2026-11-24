package com.vke.utils;

import com.vke.utils.iter.Iter;

import java.util.function.Function;

public class StringUtils {
    public static <T> String joinNiceLanguage(T[] objects, boolean useAnd) {
        return joinNiceLanguage(objects, useAnd, Object::toString);
    }

    public static <T> String joinNiceLanguage(T[] objects, boolean useAnd, Function<T, String> toRepr) {
        if (objects.length == 0) return "";
        StringBuilder builder = new StringBuilder()
                .append('\'')
                .append(toRepr.apply(objects[0]))
                .append('\'');

        for (int i = 1; i < objects.length; i++) {
            if (i == objects.length - 1) {
                if (useAnd) {
                    builder.append(" and ");
                } else {
                    builder.append(" or ");
                }
            } else {
                builder.append(", ");
            }
            builder.append('\'')
                    .append(toRepr.apply(objects[i]))
                    .append('\'');
        }
        return builder.toString();
    }

    public static Character[] charObjs(String s) {
        return Iter.of(s.chars().boxed())
                .<Character>cast()
                .toArray();
    }
}