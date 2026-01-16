package com.vke.utils.serialize.impl.defaults;

import com.vke.utils.serialize.Loader;
import com.vke.utils.serialize.Saver;
import com.vke.utils.serialize.Serializer;
import com.vke.utils.serialize.exec.LoadException;
import com.vke.utils.serialize.exec.SaveException;

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
