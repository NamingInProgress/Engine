package com.vke.api.rendering.abstraction.renderer.data;

import com.vke.api.rendering.abstraction.renderer.enums.CompareOp;
import com.vke.api.rendering.abstraction.renderer.enums.Filter;
import com.vke.utils.io.Disposable;

public interface Sampler extends Disposable {

    record Description(Filter magFilter, Filter minFilter, CompareOp compareOp) {
        public Description(Filter magFilter, Filter minFilter) { this(magFilter, minFilter, null); }
    }

    long getHandle();

}
