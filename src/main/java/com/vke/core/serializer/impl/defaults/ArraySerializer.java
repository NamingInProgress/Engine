package com.vke.core.serializer.impl.defaults;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.core.serializer.LoadException;
import com.vke.core.serializer.SaveException;

import java.lang.reflect.Array;

public class ArraySerializer<T> implements Serializer<T[]> {
    private final Class<T> clazz;

    public ArraySerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<?> getObjectClass() {
        return clazz;
    }

    @Override
    public void save(T[] value, Saver saver) throws SaveException {
        saver.saveInt(value.length);
        for (T t : value) {
            Serializer.saveObject(t, saver);
        }
    }

    @Override
    public T[] load(Loader loader) throws LoadException {
        int length = loader.loadInt();
        @SuppressWarnings("unchecked")
        T[] array = (T[]) Array.newInstance(clazz, length);
        for (int i = 0; i < length; i++) {
            array[i] = Serializer.loadObject(clazz, loader);
        }
        return array;
    }
}
