package com.vke.core.vkz.types;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.utils.exception.LoadException;
import com.vke.utils.exception.SaveException;

import java.util.Arrays;

public class VkzArray<T> {
    private final Class<T> elementClass;
    private final T[] template;

    private T[] elements;

    public VkzArray(Class<T> elementClass, T[] template) {
        this.elementClass = elementClass;
        this.template = template;
    }

    public T[] elements() {
        return elements;
    }

    public int length() {
        return elements != null ? elements.length : 0;
    }

    public void load(Loader loader) throws LoadException {
        int length = loader.loadShort();
        T[] actualArray = Arrays.copyOf(template, length);
        for (int i = 0; i < length; i++) {
            actualArray[i] = Serializer.loadObject(elementClass, loader);
        }
        this.elements = actualArray;
    }

    public void save(Saver saver) throws SaveException {
        short length = (short) elements.length;
        saver.saveShort(length);
        for (int i = 0; i < length; i++) {
            Serializer.saveObject(elements[i], saver);
        }
    }
}
