package com.vke.core.color.convert;

import com.vke.core.color.Color;

public interface ColorConversion2Way<A extends Color, B extends Color> extends ColorConversion<A, B> {
    A convertBack(B color);

    default ColorConversion2Way<B, A> back() {
        return new Back<>(this);
    }

    class Back<A extends Color, B extends Color> implements ColorConversion2Way<B, A> {
        private final ColorConversion2Way<A, B> source;

        public Back(ColorConversion2Way<A, B> source) {
            this.source = source;
        }

        @Override
        public A convert(B b) {
            return source.convertBack(b);
        }

        @Override
        public B convertBack(A color) {
            return source.convert(color);
        }
    }
}
