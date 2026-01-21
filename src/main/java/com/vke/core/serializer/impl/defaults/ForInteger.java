package com.vke.core.serializer.impl.defaults;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

public class ForInteger implements Serializer<Integer> {

    @Override
    public void save(Integer value, Saver saver) throws SaveException {
        saver.saveInt(value);
    }

    @Override
    public Integer load(Loader loader) throws LoadException {
        return loader.loadInt();
    }

    @Override
    public Class<?> getObjectClass() {
        return Integer.class;
    }
}
