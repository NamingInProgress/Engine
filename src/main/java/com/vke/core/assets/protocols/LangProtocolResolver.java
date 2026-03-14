package com.vke.core.assets.protocols;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.StageFilter;
import com.vke.api.assets.pipeline.apis.ProtocolResolver;
import com.vke.api.language.Language;
import com.vke.api.language.LanguageParser;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.core.assets.handles.LanguageAssetHandle;
import com.vke.utils.Identifier;
import com.vke.utils.Location;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

public class LangProtocolResolver implements ProtocolResolver<Language> {
    @Override
    public boolean checkProtocolContent(StageFilter filter, StageElement stageElement) throws AssetPipelineException {
        String resolved = resolveUri(filter.getUri(), stageElement);
        return filter.applyForString(resolved);
    }

    @Override
    public String resolveUri(URI uri, StageElement stageElement) throws AssetPipelineException {
        String selector = uri.getAuthority();
        String path = StageFilter.getPathOfURI(uri);
        return switch (selector) {
            case "item" -> getItem(resolveData(stageElement), path);
            default -> throw AssetPipelineException.unknownSelector("lang", selector);
        };
    }

    private String getItem(Language language, String path) {
        return language.find(new Location(path));
    }

    @Override
    public AssetHandle<?> createHandle(StageElement element) throws AssetPipelineException {
        Object data = element.getData();
        if (data instanceof Identifier identifier) {
            return new LanguageAssetHandle(identifier);
        } else if (data instanceof Language language) {
            return new LanguageAssetHandle(language);
        }
        throw new AssetPipelineException("Cannot create AssetHandle from StageElement!");
    }

    @Override
    public Language resolveData(StageElement element) throws AssetPipelineException {
        this.verifyProtocol("protocol", element.getProtocol(), "lang");
        Object data = element.getData();
        if (data instanceof Language language) {
            return language;
        } else if (data instanceof Identifier identifier){
            try {
                ConfigDocument document = ConfigDocument.parseIdentifier(identifier);
                return LanguageParser.parseFromConfig(document);
            } catch (IOException e) {
                throw new AssetPipelineException(e);
            }
        }

        throw new AssetPipelineException("Cannot resolve data from StageElement!");
    }
}