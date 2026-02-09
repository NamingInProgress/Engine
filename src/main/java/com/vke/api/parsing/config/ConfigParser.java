package com.vke.api.parsing.config;

public interface ConfigParser {
    void setSource(char[] source);

    ConfigDocument parse() throws ConfigParseException;

    class ConfigParseException extends Exception {
        public ConfigParseException(String message) {
            super(message);
        }

        public ConfigParseException(Exception e) {
            super(e);
        }
    }
}
