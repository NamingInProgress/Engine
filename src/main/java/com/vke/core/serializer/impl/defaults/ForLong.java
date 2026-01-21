package com.vke.core.serializer.impl.defaults;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

public class ForLong implements Serializer<Long> {

    @Override
    public void save(Long value, Saver saver) throws SaveException {
        saver.saveLong(value);
    }

    @Override
    public Long load(Loader loader) throws LoadException {
        return loader.loadLong();
    }

    @Override
    public Class<?> getObjectClass() {
        return Long.class;
    }
}
