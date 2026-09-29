package com.vke.core.vz2.io;

import java.io.IOException;

public interface VzLoader<C> {
    C load(RAInputStream stream) throws IOException;
}
