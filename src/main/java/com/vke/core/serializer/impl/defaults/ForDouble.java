package com.vke.core.serializer.impl.defaults;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

public class ForDouble implements Serializer<Double> {

    @Override
    public void save(Double value, Saver saver) throws SaveException {
        saver.saveDouble(value);
    }

    @Override
    public Double load(Loader loader) throws LoadException {
        return loader.loadDouble();
    }

    @Override
    public Class<?> getObjectClass() {
        return Double.class;
    }
}
