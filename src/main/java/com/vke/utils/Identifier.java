package com.vke.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class Identifier {

    private final String namespace, path;
    private final String combined;

    public Identifier(String path) {
        this.namespace = "vke";
        this.path = path;
        this.combined = namespace.concat("/").concat(path);
    }

    public Identifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
        this.combined = namespace.concat("/").concat(path);
    }

    public static Identifier empty() {
        return new Identifier("", "");
    }
    /// namespace:path
    public static Identifier of(String literal) {
        String[] parts = literal.split(":", 2);
        if (parts.length > 1) {
            return new Identifier(parts[0], parts[1]);
        } else {
            return new Identifier(parts[0]);
        }
    }

    public String getNamespace() { return this.namespace; }
    public String getPath() { return this.path; }

    public InputStream asInputStream() throws IOException {
        var s = Identifier.class.getClassLoader().getResourceAsStream(combined);
        if (s == null) throw new IOException("Failed to find file at " + combined);
        return s;
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    public String toSpecialVkzFormatCuzItsBad() { return (namespace + "_" + path).replaceAll("/", "_"); }

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
