package com.vke.core.parsing.config.schema.vks.doc;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.utils.Utils;

public class VksParser implements ConfigParser {
    private char[] source;

    @Override
    public void setSource(char[] source) {
        this.source = source;
    }

    @Override
    public ConfigDocument parse(int flags) throws ConfigParseException {
        return Utils.chainExceptions(() -> new VksDocument(source));
    }
}
