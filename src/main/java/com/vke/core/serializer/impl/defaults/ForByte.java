package com.vke.core.serializer.impl.defaults;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

public class ForByte implements Serializer<Byte> {

    @Override
    public void save(Byte value, Saver saver) throws SaveException {
        saver.saveByte(value);
    }

    @Override
    public Byte load(Loader loader) throws LoadException {
        return loader.loadByte();
    }

    @Override
    public Class<?> getObjectClass() {
        return Byte.class;
    }
}

