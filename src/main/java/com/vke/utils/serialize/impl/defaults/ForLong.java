package com.vke.utils.serialize.impl.defaults;

import com.vke.utils.serialize.Loader;
import com.vke.utils.serialize.Saver;
import com.vke.utils.serialize.Serializer;
import com.vke.utils.serialize.exec.LoadException;
import com.vke.utils.serialize.exec.SaveException;

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
