package com.vke.api.app;

import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;

public interface Namespace {
    String getName();

    Identifier id(String value);

    FileIdentifier fid(String value);

    static Namespace of(String namespace) {
        return new Simple(namespace);
    }

    class Simple implements Namespace {
        private final String ns;

        private Simple(String ns) {
            this.ns = ns;
        }

        @Override
        public String getName() {
            return ns;
        }

        @Override
        public Identifier id(String value) {
            return Identifier.ofSafe(value, ns);
        }

        @Override
        public FileIdentifier fid(String value) {
            return FileIdentifier.ofSafe(value, ns);
        }
    }
}
