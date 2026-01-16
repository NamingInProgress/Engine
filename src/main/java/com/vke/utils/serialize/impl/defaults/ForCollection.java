package com.vke.utils.serialize.impl.defaults;

import com.vke.utils.serialize.Loader;
import com.vke.utils.serialize.Saver;
import com.vke.utils.serialize.Serializer;
import com.vke.utils.serialize.exec.LoadException;
import com.vke.utils.serialize.exec.SaveException;

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
