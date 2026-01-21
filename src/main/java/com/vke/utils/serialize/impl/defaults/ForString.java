package com.vke.utils.serialize.impl.defaults;

import com.vke.utils.serialize.Loader;
import com.vke.utils.serialize.Saver;
import com.vke.utils.serialize.Serializer;
import com.vke.utils.serialize.exec.LoadException;
import com.vke.utils.serialize.exec.SaveException;

import java.nio.charset.StandardCharsets;

public class ForString implements Serializer<String> {
    @Override
    public void save(String value, Saver saver) throws SaveException {
        saver.saveByteArray(value.getBytes(StandardCharsets.UTF_16));
    }

    @Override
    public String load(Loader loader) throws LoadException {
        return new String(loader.loadByteArray(), StandardCharsets.UTF_16);
    }

    @Override
    public Class<?> getObjectClass() {
        return String.class;
    }
}
