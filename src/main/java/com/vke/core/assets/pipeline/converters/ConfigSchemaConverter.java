package com.vke.core.assets.pipeline.converters;

import com.vke.api.assets.Protocols;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetData;

public class ConfigSchemaConverter implements AssetConverter {
    @Override
    public String from() {
        return Protocols.CONFIG;
    }

    @Override
    public String to() {
        return Protocols.SCHEMA;
    }

    @Override
    public AssetData performConversion(Context context, StageElement input, ConfigArrayNode arguments) throws AssetException {
        ConfigDocument doc = input.getAssetData().getDataAs();
        try {
            ConfigSchema schema = ConfigSchema.readVke(doc, input.getAssetName().getPath());
            return new AssetData(Protocols.SCHEMA, schema);
        } catch (ConfigParser.ConfigParseException e) {
            throw new RuntimeException(e);
        }
    }
}
