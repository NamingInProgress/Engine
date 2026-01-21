package com.vke.utils.serialize;

import com.vke.utils.serialize.exec.LoadException;
import com.vke.utils.serialize.exec.SaveException;
import com.vke.utils.serialize.impl.defaults.DefaultSerializers;

public interface Serializer<T> {
    Class<?> getObjectClass();

    void save(T value, Saver saver) throws SaveException;
    T load(Loader loader) throws LoadException;

    static <U> void registerSerializerFor(Class<U> clazz, Serializer<?> serializer) {
        Serializers.SERIALIZERS.put(clazz, serializer);
    }

    static <U> Serializer<U> findSerializer(Class<U> clazz) {
        Serializer<?> s = Serializers.SERIALIZERS.get(clazz);
        if (s != null) {
            return (Serializer<U>) s;
        }
        //no direct serializer found, check if an interface serializer exists.
        //example: ArrayList might not have a serializer but Collection does
        for (Serializer<?> candidate : Serializers.SERIALIZERS.values()) {
            Class<?> candClass = candidate.getObjectClass();
            if (candClass.isAssignableFrom(clazz)) {
                return (Serializer<U>) candidate;
            }
        }
        return null;
    }

    static void saveCheckNull(Object value, Saver saver) throws SaveException {
        saver.saveBits(1, value == null ? 1 : 0);
    }

    static boolean loadCheckNull(Loader loader) throws LoadException {
        return (loader.loadBits(1) & 1) == 1;
    }

    static <U> void saveObject(U value, Saver saver) throws SaveException {
        DefaultSerializers.checkRegistration();

        try {
            saveCheckNull(value, saver);
            if (value == null) return;
            Serializer<U> s = (Serializer<U>) findSerializer(value.getClass());
            s.save(value, saver);
        } catch (NullPointerException | ClassCastException ignore) {
            throw new SaveException("No matching Serializer found for " + value.getClass().getName());
        }
    }

    static <U> U loadObject(Class<?> clazz, Loader loader) throws LoadException {
        DefaultSerializers.checkRegistration();

        if (loadCheckNull(loader)) return null;
        try {
            Serializer<U> s = (Serializer<U>) findSerializer(clazz);
            return s.load(loader);
        } catch (NullPointerException | ClassCastException ignore) {
            throw new LoadException("No matching Serializer found for " + clazz.getName());
        }
    }

    static <U> void saveFatObject(U value, Saver saver) throws SaveException {
        DefaultSerializers.checkRegistration();

        try {
            saveCheckNull(value, saver);
            if (value == null) return;
            Class<?> clazz = value.getClass();
            Serializer<U> s = (Serializer<U>) findSerializer(clazz);
            String name = clazz.getName();

            Serializer<String> str = findSerializer(String.class);
            str.save(name, saver);

            s.save(value, saver);
        } catch (NullPointerException | ClassCastException ignore) {
            throw new SaveException("No matching Serializer found for " + value.getClass().getName());
        }
    }

    static Object loadFatObject(Loader loader) throws LoadException {
        return loadFatObject(loader, Serializer.class.getClassLoader());
    }

    static Object loadFatObject(Loader loader, ClassLoader classLoader) throws LoadException {
        DefaultSerializers.checkRegistration();

        if (loadCheckNull(loader)) return null;
        String className = "<unknown>";
        try {
            Serializer<String> str = findSerializer(String.class);
            className = str.load(loader);
            Class<?> clazz = Class.forName(className, false, classLoader);
            Serializer<?> s = findSerializer(clazz);
            return s.load(loader);
        } catch (NullPointerException | ClassCastException ignore) {
            throw new LoadException("No loader found for class " + className);
        } catch (ClassNotFoundException e) {
            throw new LoadException("Illegal class name loaded for fat object!");
        }
    }
}
