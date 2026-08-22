package com.vke.core;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.core.serializer.LoadException;
import com.vke.core.serializer.SaveException;

import java.util.Objects;

public class Identifier {
    private final String namespace;
    private final String path;

    private final String toString, combined;

    public Identifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
        this.toString = buildToString();
        this.combined = buildPath();
    }

    private String buildToString() {
        return namespace + ":" + path;
    }

    private String buildPath() {
        return namespace + "/" + path;
    }

    //example: "vke:my/path"
    public static Identifier of(String value) {
        return Identifier.ofSafe(value, VKEngine.VKE_NAMESPACE);
    }

    public static Identifier ofSafe(String value, String backupNamespace) {
        String[] parts = value.split(":", 2);
        if (parts.length == 1) {
            return new Identifier(backupNamespace, parts[0]);
        } else if (parts.length == 2) {
            return new Identifier(parts[0], parts[1]);
        } else {
            return new Identifier(backupNamespace, "");
        }
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    public String getCombinedPath() {
        return combined;
    }

    public Identifier extend(String extension) {
        return new Identifier(namespace, path + "/" + extension);
    }

    public Identifier extendRaw(String extension) {
        return new Identifier(namespace, path + extension);
    }

    public Identifier strip() {
        String path = getPath();
        int id = path.lastIndexOf('/');
        if (id >= 0) {
            path = path.substring(id + 1);
            return new Identifier(namespace, path);
        }
        return new Identifier(namespace, path);
    }

    @Override
    public String toString() {
        return toString;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Identifier id) {
            return Objects.equals(toString, id.toString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(toString);
    }

    public static void registerSerializers() {
        Serializer.registerSerializerFor(Identifier.class, new S());
    }

    private static class S implements Serializer<Identifier> {

        @Override
        public Class<?> getObjectClass() {
            return Identifier.class;
        }

        @Override
        public void save(Identifier value, Saver saver) throws SaveException {
            Serializer.saveObject(value.namespace, saver);
            Serializer.saveObject(value.path, saver);
        }

        @Override
        public Identifier load(Loader loader) throws LoadException {
            String namespace = Serializer.loadObject(String.class, loader);
            String path = Serializer.loadObject(String.class, loader);
            return new Identifier(namespace, path);
        }
    }
}
