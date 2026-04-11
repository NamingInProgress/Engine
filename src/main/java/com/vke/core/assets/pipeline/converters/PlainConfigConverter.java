package com.vke.core.assets.pipeline.converters;

import com.vke.api.assets.Protocols;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.utils.io.FileUtils;

public class PlainConfigConverter implements AssetConverter {
    @Override
    public String from() {
        return Protocols.PLAIN;
    }

    @Override
    public String to() {
        return Protocols.CONFIG;
    }

    @Override
    public AssetData performConversion(Context context, StageElement input, ConfigArrayNode arguments) throws AssetException {
        try {
            String str = input.getAssetData().getDataAs();
            String filename = FileUtils.getFileName(input.getPath());
            ConfigParser parser = ConfigParser.forFileType(filename);
            if (parser == null) throw new AssetException("No suitable config parser found for " + filename);
            parser.setSource(str.toCharArray());
            ConfigDocument document = parser.parse(ConfigParser.PARSE_LITERALS | ConfigParser.ATTRIBS_TO_FIELDS);
            return AssetData.config(document);
        } catch (ConfigParser.ConfigParseException e) {
            throw new AssetException(e);
        }
    }
}
