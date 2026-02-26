package com.vke.api.rendering.abstraction.data;

import com.vke.api.rendering.abstraction.enums.CompareOp;
import com.vke.api.rendering.abstraction.enums.Filter;
import com.vke.utils.Disposable;

public interface Sampler extends Disposable {

    record Description(Filter magFilter, Filter minFilter, CompareOp compareOp) {
        public Description(Filter magFilter, Filter minFilter) { this(magFilter, minFilter, null); }
    }

    long getHandle();

}
