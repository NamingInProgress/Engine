package com.vke.api.framable;

import com.vke.utils.collection.AbstractGlossary;
import com.vke.utils.iter.Iter;

public interface Framable {

    default void preFrame() {}
    default void preRendering() {}
    default void onDraw() {}
    default void postRendering() {}
    default void postFrame() {}

    class Glossary extends AbstractGlossary<Framable> implements CompoundFramable {
        @Override
        public Iter<Framable> children() {
            return Iter.of(entries);
        }
    }
}
