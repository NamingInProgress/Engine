package com.vke.api.app;

import com.vke.core.rendering.draw.DrawContext;
import com.vke.utils.collection.AbstractGlossary;
import com.vke.utils.iter.Iter;

public interface Framable {

    default void preFrame() {}
    default void preRendering(DrawContext ctx) {}
    default void onDraw(DrawContext ctx) {}
    default void postRendering(DrawContext ctx) {}
    default void postFrame() {}

    class Glossary extends AbstractGlossary<Framable> implements CompoundFramable {
        @Override
        public Iter<Framable> children() {
            return Iter.of(entries);
        }
    }
}
