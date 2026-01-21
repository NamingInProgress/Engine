package com.vke.core.serializer.impl.defaults;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

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
