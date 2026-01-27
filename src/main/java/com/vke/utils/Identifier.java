package com.vke.utils;

import java.io.InputStream;
import java.util.Objects;

public class Identifier {

    private final String namespace, path;

    public Identifier(String path) {
        this.namespace = "vke";
        this.path = path;
    }

    public Identifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public String getNamespace() { return this.namespace; }
    public String getPath() { return this.path; }

    public InputStream asInputStream() { return Identifier.class.getClassLoader().getResourceAsStream("%s/%s".formatted(namespace, path)); }

    @Override
    public String toString() {
        return path + ":" + namespace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.namespace, this.path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;            // same reference
        if (o == null || getClass() != o.getClass()) return false; // different class
        Identifier myKey = (Identifier) o;
        // compare the two strings
        return Objects.equals(namespace, myKey.namespace) &&
                Objects.equals(path, myKey.path);
    }

}
