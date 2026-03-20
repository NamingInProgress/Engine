package com.vke.utils.io;

import com.vke.core.VKEngine;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Identifier {

    private final String namespace, path;
    private final String combined;

    public Identifier(String path) {
        this.namespace = VKEngine.VKE_NAMESPACE;
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

    public Identifier extend(String additionalPath) {
        return new Identifier(this.namespace, this.path + "/" + additionalPath);
    }

    public Iter<Identifier> walkFiles() {
        return walkFiles(Integer.MAX_VALUE);
    }

    public Iter<Identifier> walkDirectories() {
        return walkDirectories(Integer.MAX_VALUE);
    }

    public Iter<Identifier> walkFiles(int maxDepth) {
        return FileUtils.getRelativePaths(combined, maxDepth).filterMap(wf -> Option.useIf(wf.isFile(), () -> this.extend(wf.name())));
    }

    public Iter<Identifier> walkDirectories(int maxDepth) {
        return FileUtils.getRelativePaths(combined, maxDepth).filterMap(wf -> Option.useIf(!wf.isFile(), () -> this.extend(wf.name())));
    }

    public String getNamespace() { return this.namespace; }
    public String getPath() { return this.path; }

    public InputStream asInputStream() throws IOException {
        var s = Identifier.class.getClassLoader().getResourceAsStream(combined);
        if (s == null) throw new IOException("Failed to find file at " + combined);
        return s;
    }

    public Path toPath() {
        return Paths.get(combined).normalize();
    }

    public boolean existsFile() {
        ClassLoader cl = getClass().getClassLoader();
        return cl.getResource(combined) != null;
    }

    public String getExtension() {
        return path.substring(path.lastIndexOf('.') + 1);
    }

    public String getExtensionLower() {
        return getExtension().toLowerCase();
    }

    public Identifier strip() {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0) {
            return new Identifier(namespace, path.substring(lastSlash + 1));
        }
        //new instance to not confuse any usages
        return new Identifier(namespace, path);
    }

    @Override
    public String toString() {
        return combined;
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
