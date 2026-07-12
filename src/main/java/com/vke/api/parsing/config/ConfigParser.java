package com.vke.api.parsing.config;

import com.vke.core.parsing.config.json.JsonParser;
import com.vke.core.parsing.config.xml.XmlParser;
import com.vke.utils.Utils;

public interface ConfigParser {
    /**
     * Makes it so that all literals are converted to strings. That means that only {@link com.vke.api.parsing.config.node.ConfigValueNode ConfigValueNode}
     * instances will exist!
     */
    int STRINGS_ONLY = 1;

    /**
     * Strings will be converted to number/boolean literals if possible. This is useful for xml support, since xml natively doesnt support number or boolean literals.
     */
    int PARSE_LITERALS = 1 << 1;

    /**
     * Will convert attributes to new fields. In practice, imagine the following XML snippet:
     *
     * <pre>{@code
     * <root>
     *   <tag v1="1" v2="good morning!">Hello</tag>
     * </root>
     * }</pre>
     *
     * With this option enabled, it will be represented like this:
     *
     * <pre>{@code
     * <root>
     *   <tag>
     *     <v1>1</v1>
     *     <v2>good morning!</v2>
     *     Hello
     *   </tag>
     * </root>
     * }</pre>
     *
     * This is useful to allow interchangeability of XML and JSON
     * without requiring complex parsing logic.
     */
    int ATTRIBS_TO_FIELDS = 1 << 2;

    void setSource(char[] source);

    default ConfigDocument parse() throws ConfigParseException {
        return parse(0);
    }

    ConfigDocument parse(int flags) throws ConfigParseException;

    static ConfigParser forFileType(String filename) {
        if (Utils.seqContainsIgnoreCase(filename, "json")) {
            return new JsonParser();
        }
        if (Utils.seqContainsAnyIgnoreCase(filename, "xml", "vka", "vcl")) {
            return new XmlParser();
        }
        return null;
    }

    class ConfigParseException extends Exception {
        public ConfigParseException(String message) {
            super(message);
        }

        public ConfigParseException(Exception e) {
            super(e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
