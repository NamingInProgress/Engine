package com.vke.utils;

import java.io.InputStream;

public class Identifier {

    private final String namespace, path;

    public Identifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public String getNamespace() { return this.namespace; }
    public String getPath() { return this.path; }

    public InputStream asInputStream() { return Identifier.class.getClassLoader().getResourceAsStream("%s/%s".formatted(namespace, path)); }

}
