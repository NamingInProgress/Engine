package com.vke.utils.serialize.impl.defaults;

import com.vke.utils.serialize.Loader;
import com.vke.utils.serialize.Saver;
import com.vke.utils.serialize.Serializer;
import com.vke.utils.serialize.exec.LoadException;
import com.vke.utils.serialize.exec.SaveException;

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
