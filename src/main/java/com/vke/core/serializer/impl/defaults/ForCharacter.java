package com.vke.core.serializer.impl.defaults;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.core.serializer.LoadException;
import com.vke.core.serializer.SaveException;

public class ForCharacter implements Serializer<Character> {

    @Override
    public void save(Character value, Saver saver) throws SaveException {
        saver.saveChar(value);
    }

    @Override
    public Character load(Loader loader) throws LoadException {
        return loader.loadChar();
    }

    @Override
    public Class<?> getObjectClass() {
        return Character.class;
    }
}
