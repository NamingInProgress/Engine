package com.vke.core.serializer.impl.defaults;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

public class ForFloat implements Serializer<Float> {

    @Override
    public void save(Float value, Saver saver) throws SaveException {
        saver.saveFloat(value);
    }

    @Override
    public Float load(Loader loader) throws LoadException {
        return loader.loadFloat();
    }

    @Override
    public Class<?> getObjectClass() {
        return Float.class;
    }
}
