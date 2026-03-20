package com.vke.api.serializer;

import com.vke.api.registry.VKERegistries;
import com.vke.core.serializer.LoadException;
import com.vke.core.serializer.SaveException;
import com.vke.core.serializer.impl.defaults.DefaultSerializers;

import static com.vke.core.VKEngine.REGISTRATE;

public interface Serializer<T> {
    Class<?> getObjectClass();

    void save(T value, Saver saver) throws SaveException;
    T load(Loader loader) throws LoadException;

    static <U> void registerSerializerFor(Class<U> clazz, Serializer<?> serializer) {
        REGISTRATE.serializer(clazz, serializer);
    }

    static <U> Serializer<U> findSerializer(Class<U> clazz) {
        Serializer<?> s = VKERegistries.SERIALIZERS.get(clazz);
        if (s != null) {
            return (Serializer<U>) s;
        }
        //no direct serializer found, check if an interface serializer exists.
        //example: ArrayList might not have a serializer but Collection does
        for (Serializer<?> candidate : VKERegistries.SERIALIZERS.values()) {
            Class<?> candClass = candidate.getObjectClass();
            if (candClass.isAssignableFrom(clazz)) {
                return (Serializer<U>) candidate;
            }
        }
        return null;
    }

    static void saveCheckNull(Object value, Saver saver, boolean enable) throws SaveException {
        if (enable) saver.saveBits(1, value == null ? 1 : 0);
    }

    static boolean loadCheckNull(Loader loader, boolean enable) throws LoadException {
        if (!enable) return false;
        return (loader.loadBits(1) & 1) == 1;
    }

    static <U> void saveObject(U value, Saver saver) throws SaveException {
        saveObject(value, saver, true);
    }

    static <U> void saveObject(U value, Saver saver, boolean nullCheck) throws SaveException {
        DefaultSerializers.checkRegistration();

        try {
            saveCheckNull(value, saver, nullCheck);
            if (value == null) return;
            Serializer<U> s = (Serializer<U>) findSerializer(value.getClass());
            if (s == null) throw new ClassCastException();
            s.save(value, saver);
        } catch (ClassCastException ignore) {
            throw new SaveException("No matching Serializer found for " + value.getClass().getName());
        }
    }

    static <U> U loadObject(Class<?> clazz, Loader loader) throws LoadException {
        return loadObject(clazz, loader, true);
    }

    static <U> U loadObject(Class<?> clazz, Loader loader, boolean nullCheck) throws LoadException {
        DefaultSerializers.checkRegistration();

        if (loadCheckNull(loader, nullCheck)) return null;
        try {
            Serializer<U> s = (Serializer<U>) findSerializer(clazz);
            if (s == null) throw new ClassCastException();
            return s.load(loader);
        } catch (ClassCastException ignore) {
            throw new LoadException("No matching Serializer found for " + clazz.getName());
        }
    }

    static <U> void saveFatObject(U value, Saver saver) throws SaveException {
        saveFatObject(value, saver, true);
    }

    static <U> void saveFatObject(U value, Saver saver, boolean nullCheck) throws SaveException {
        DefaultSerializers.checkRegistration();

        try {
            saveCheckNull(value, saver, nullCheck);
            if (value == null) return;
            Class<?> clazz = value.getClass();
            Serializer<U> s = (Serializer<U>) findSerializer(clazz);
            if (s == null) throw new ClassCastException();
            String name = clazz.getName();

            Serializer<String> str = findSerializer(String.class);
            str.save(name, saver);

            s.save(value, saver);
        } catch (ClassCastException ignore) {
            throw new SaveException("No matching Serializer found for " + value.getClass().getName());
        }
    }

    static Object loadFatObject(Loader loader, boolean nullCheck) throws LoadException {
        return loadFatObject(loader, Serializer.class.getClassLoader(), nullCheck);
    }

    static Object loadFatObject(Loader loader, ClassLoader classLoader) throws LoadException {
        return loadFatObject(loader, classLoader, true);
    }

    static Object loadFatObject(Loader loader) throws LoadException {
        return loadFatObject(loader, Serializer.class.getClassLoader(), true);
    }

    static Object loadFatObject(Loader loader, ClassLoader classLoader, boolean nullCheck) throws LoadException {
        DefaultSerializers.checkRegistration();

        if (loadCheckNull(loader, nullCheck)) return null;
        String className = "<unknown>";
        try {
            Serializer<String> str = findSerializer(String.class);
            className = str.load(loader);
            Class<?> clazz = Class.forName(className, false, classLoader);
            Serializer<?> s = findSerializer(clazz);
            if (s == null) throw new ClassCastException();
            return s.load(loader);
        } catch (ClassCastException ignore) {
            throw new LoadException("No loader found for class " + className);
        } catch (ClassNotFoundException e) {
            throw new LoadException("Illegal class name loaded for fat object!");
        }
    }
}
