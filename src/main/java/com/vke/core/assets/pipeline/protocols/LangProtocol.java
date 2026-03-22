package com.vke.core.assets.pipeline.protocols;

import com.vke.api.assets.Protocols;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.assets.language.Language;
import com.vke.core.assets.language.LanguageParser;
import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.utils.io.Identifier;

public class LangProtocol implements AssetProtocol<Language> {
    @Override
    public String getProtocolName() {
        return Protocols.LANG;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetPipelineException {
        if (uri.getSelector().equals("key")) {
            Language language = data.getDataAs();
            return AssetData.plain(language.find(uri.getPath()));
        } else {
            throw new AssetPipelineException("Illegal selector for protocol 'lang': only 'key' is allowed!");
        }
    }

    @Override
    public Loader getLoader() {
        return new LangProtocolLoader();
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        return false;
    }

    public static class LangProtocolLoader implements Loader {
        @Override
        public AssetData load(Context context, Identifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetPipelineException {
            ConfigDocument document = new ConfigProtocol.ConfigProtocolLoader().load(context, identifier, executionTarget).getDataAs();
            return AssetData.lang(LanguageParser.parseFromConfig(document));
        }
    }
}
