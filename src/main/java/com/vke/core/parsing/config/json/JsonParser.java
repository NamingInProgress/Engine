package com.vke.core.parsing.config.json;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.file.deflate.decompress.BitUtils;
import com.vke.core.parsing.ParseUtils;
import com.vke.core.parsing.SourceCursor;
import com.vke.core.parsing.config.json.nodes.*;
import com.vke.core.parsing.config.json.tokens.JsonToken;
import com.vke.core.parsing.config.json.tokens.JsonTokenizer;
import com.vke.utils.exception.Unreachable;

import java.util.ArrayList;
import java.util.List;

public class JsonParser implements ConfigParser {
    private JsonTokenizer tokenizer;

    @Override
    public void setSource(char[] source) {
        this.tokenizer = new JsonTokenizer(new SourceCursor(source, 0));
    }

    @Override
    public ConfigDocument parse(int flags) throws ConfigParseException {
        return new JsonDocument(parseNode(flags));
    }

    private ConfigNode parseNode(int flags) throws ConfigParseException {
        try {
            JsonToken next = tokenizer.nextToken();
            if (next.getType() == JsonToken.Type.LBrace) {
                //object
                JsonObjectNode objectNode = new JsonObjectNode();
                next = tokenizer.nextToken();
                if (next.getType() == JsonToken.Type.RBrace) {
                    return objectNode;
                }
                tokenizer.putback(next);
                while (true) {
                    String key = tokenizer.expectToken(JsonToken.Type.StrLit).value();
                    tokenizer.expectToken(JsonToken.Type.Colon);
                    ConfigNode value = parseNode(flags);
                    objectNode.addNode(key, value);
                    JsonToken comma = tokenizer.nextToken();
                    if (comma.getType() != JsonToken.Type.Comma) {
                        tokenizer.putback(comma);
                        tokenizer.expectToken(JsonToken.Type.RBrace);
                        break;
                    }
                }
                return objectNode;
            }
            if (next.getType() == JsonToken.Type.LBrack) {
                //array
                next = tokenizer.nextToken();
                if (next.getType() == JsonToken.Type.RBrack) {
                    return new JsonArrayNode(new ConfigNode[0]);
                }
                tokenizer.putback(next);
                List<ConfigNode> values = new ArrayList<>();
                while (true) {
                    ConfigNode value = parseNode(flags);
                    values.add(value);
                    next = tokenizer.nextToken();
                    if (next.getType() != JsonToken.Type.Comma) {
                        tokenizer.putback(next);
                        tokenizer.expectToken(JsonToken.Type.RBrack);
                        break;
                    }
                }
                return new JsonArrayNode(values.toArray(ConfigNode[]::new));
            }
            if (next.getType() == JsonToken.Type.StrLit) {
                //value
                if (BitUtils.bitsContains(flags, ConfigParser.PARSE_LITERALS)) {
                    Object value = ParseUtils.interpretString(next.value());
                    return switch (value) {
                        case Float f -> new JsonNumberNode(f);
                        case Boolean f -> new JsonBooleanNode(f);
                        case String f -> new JsonValueNode(f);
                        default -> throw new Unreachable();
                    };
                }
                return new JsonValueNode(next.value());
            }
            if (next.getType() == JsonToken.Type.BoolLit) {
                if (BitUtils.bitsContains(flags, ConfigParser.STRINGS_ONLY)) {
                    boolean val = next.value();
                    return new JsonValueNode(String.valueOf(val));
                }
                return new JsonBooleanNode(next.value());
            }
            if (next.getType() == JsonToken.Type.NumLit) {
                if (BitUtils.bitsContains(flags, ConfigParser.STRINGS_ONLY)) {
                    float val = next.value();
                    return new JsonValueNode(String.valueOf(val));
                }
                return new JsonNumberNode(next.value());
            }
            throw new ConfigParseException("Unexpected token: " + next.getType());
        } catch (SourceCursor.EOF eof) {
            throw new ConfigParseException("Unexpected End of input");
        } catch (ConfigParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigParseException(e);
        }
    }
}
