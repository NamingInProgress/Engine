package com.vke.core.serializer.impl.defaults;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.core.serializer.LoadException;
import com.vke.core.serializer.SaveException;

public class ForShort implements Serializer<Short> {

    @Override
    public void save(Short value, Saver saver) throws SaveException {
        saver.saveShort(value);
    }

    @Override
    public Short load(Loader loader) throws LoadException {
        return loader.loadShort();
    }

    @Override
    public Class<?> getObjectClass() {
        return Short.class;
    }
}
