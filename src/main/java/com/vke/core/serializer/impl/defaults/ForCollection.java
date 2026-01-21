package com.vke.core.serializer.impl.defaults;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ForCollection implements Serializer<Collection<?>> {
    @Override
    public void save(Collection<?> value, Saver saver) throws SaveException {
        int size = value.size();
        saver.saveInt(size);
        if (size != 0) {
            for (Object obj : value) {
                Serializer.saveFatObject(obj, saver);
            }
        }
    }

    @Override
    public Collection<?> load(Loader loader) throws LoadException {
        int size = loader.loadInt();
        if (size == 0) {
            return List.of();
        }
        List<Object> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Object loaded = Serializer.loadFatObject(loader);
            out.add(loaded);
        }
        return out;
    }

    @Override
    public Class<?> getObjectClass() {
        return Collection.class;
    }
}
