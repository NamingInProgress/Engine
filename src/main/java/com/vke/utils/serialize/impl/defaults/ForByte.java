package com.vke.utils.serialize.impl.defaults;

import com.vke.utils.serialize.Loader;
import com.vke.utils.serialize.Saver;
import com.vke.utils.serialize.Serializer;
import com.vke.utils.serialize.exec.LoadException;
import com.vke.utils.serialize.exec.SaveException;

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

