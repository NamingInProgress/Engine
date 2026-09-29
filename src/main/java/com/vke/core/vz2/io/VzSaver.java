package com.vke.core.vz2.io;

import java.io.IOException;

public interface VzSaver {
    void save(RAOutputStream stream) throws IOException;
}
