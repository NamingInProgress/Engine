package com.vke.api.app;

import com.vke.core.rendering.draw.FrameContext;
import com.vke.utils.collection.AbstractGlossary;
import com.vke.utils.iter.Iter;

public interface Framable {

    default void preFrame() {}
    default void preRendering(FrameContext ctx) {}
    default void onDraw(FrameContext ctx) {}
    default void postRendering(FrameContext ctx) {}
    default void postFrame() {}

    class Glossary extends AbstractGlossary<Framable> implements CompoundFramable {
        @Override
        public Iter<Framable> children() {
            return Iter.of(entries);
        }
    }
}
