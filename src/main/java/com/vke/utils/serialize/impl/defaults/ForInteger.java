package com.vke.utils.serialize.impl.defaults;

import com.vke.utils.serialize.Loader;
import com.vke.utils.serialize.Saver;
import com.vke.utils.serialize.Serializer;
import com.vke.utils.serialize.exec.LoadException;
import com.vke.utils.serialize.exec.SaveException;

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
