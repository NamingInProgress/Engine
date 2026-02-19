package com.vke.api.abstraction.data;

import com.vke.api.abstraction.descriptors.CompareOp;
import com.vke.api.abstraction.descriptors.Filter;
import com.vke.utils.Disposable;

public interface Sampler extends Disposable {

    record Description(Filter magFilter, Filter minFilter, CompareOp compareOp) {
        public Description(Filter magFilter, Filter minFilter) { this(magFilter, minFilter, null); }
    }

    long getHandle();

}
