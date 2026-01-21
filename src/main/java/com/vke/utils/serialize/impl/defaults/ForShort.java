package com.vke.utils.serialize.impl.defaults;

import com.vke.utils.serialize.Loader;
import com.vke.utils.serialize.Saver;
import com.vke.utils.serialize.Serializer;
import com.vke.utils.serialize.exec.LoadException;
import com.vke.utils.serialize.exec.SaveException;

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
