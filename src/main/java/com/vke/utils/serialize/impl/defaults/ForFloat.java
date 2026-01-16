package com.vke.utils.serialize.impl.defaults;

import com.vke.utils.serialize.Loader;
import com.vke.utils.serialize.Saver;
import com.vke.utils.serialize.Serializer;
import com.vke.utils.serialize.exec.LoadException;
import com.vke.utils.serialize.exec.SaveException;

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
