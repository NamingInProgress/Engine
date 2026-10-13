package com.vke.core.ui.signal;

import com.vke.utils.iter.Iter;

public class Adapter<T> extends Signal<Iter<T>> {
    public Adapter(Iter<T> init) {
        super(init);
    }
}
