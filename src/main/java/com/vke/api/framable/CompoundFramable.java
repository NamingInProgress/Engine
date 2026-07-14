package com.vke.api.framable;

import com.vke.utils.iter.Iter;

public interface CompoundFramable extends Framable {

    Iter<Framable> children();

    @Override
    default void preFrame() { children().forEach(Framable::preFrame); }

    @Override
    default void preRendering() { children().forEach(Framable::preRendering); }

    @Override
    default void onDraw() { children().forEach(Framable::onDraw); }

    @Override
    default void postRendering() { children().forEach(Framable::postRendering); }

    @Override
    default void postFrame() { children().forEach(Framable::postFrame); }

}
