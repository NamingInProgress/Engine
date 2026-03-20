package com.vke.core.assets.pipeline.converters;

import com.vke.api.assets.Protocols;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.core.assets.language.Language;
import com.vke.core.assets.language.LanguageParser;
import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetData;

public class ConfigLangConverter implements AssetConverter {
    @Override
    public String from() {
        return Protocols.CONFIG;
    }

    @Override
    public String to() {
        return Protocols.LANG;
    }

    @Override
    public AssetData performConversion(StageElement input, ConfigArrayNode arguments) throws AssetPipelineException {
        ConfigDocument document = input.getAssetData().getDataAs();
        Language language = LanguageParser.parseFromConfig(document);
        return AssetData.lang(language);
    }
}
