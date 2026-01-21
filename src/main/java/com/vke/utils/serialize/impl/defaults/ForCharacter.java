package com.vke.utils.serialize.impl.defaults;

import com.vke.utils.serialize.Loader;
import com.vke.utils.serialize.Saver;
import com.vke.utils.serialize.Serializer;
import com.vke.utils.serialize.exec.LoadException;
import com.vke.utils.serialize.exec.SaveException;

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
