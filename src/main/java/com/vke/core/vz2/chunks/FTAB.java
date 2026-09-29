package com.vke.core.vz2.chunks;

import com.vke.core.vz2.io.RAInputStream;
import com.vke.core.vz2.io.RAOutputStream;
import com.vke.core.vz2.io.VzLoader;
import com.vke.core.vz2.io.VzSaver;

import java.io.IOException;

public class FTAB implements VzSaver {


    @Override
    public void save(RAOutputStream stream) throws IOException {

    }

    public static class Loader implements VzLoader<FTAB> {

        @Override
        public FTAB load(RAInputStream stream) throws IOException {
            return null;
        }
    }
}
