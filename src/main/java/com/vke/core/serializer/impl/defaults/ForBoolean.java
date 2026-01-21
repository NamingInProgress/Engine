package com.vke.core.serializer.impl.defaults;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

public class ForBoolean implements Serializer<Boolean> {

    @Override
    public void save(Boolean value, Saver saver) throws SaveException {
        saver.saveBoolean(value);
    }

    @Override
    public Boolean load(Loader loader) throws LoadException {
        return loader.loadBoolean();
    }

    @Override
    public Class<?> getObjectClass() {
        return Boolean.class;
    }
}
