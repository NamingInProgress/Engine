package com.vke.core.parsing.config.schema.vks.doc;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.core.FileIdentifier;
import com.vke.utils.Utils;

public class VksParser implements ConfigParser {
    private char[] source;
    private FileIdentifier identifier;

    @Override
    public void setSource(char[] source) {
        this.source = source;
    }

    @Override
    public void setFile(FileIdentifier identifier) {
        this.identifier = identifier;
    }

    @Override
    public ConfigDocument parse(int flags) throws ConfigParseException {
        return Utils.chainExceptions(() -> new VksDocument(source, identifier));
    }
}
