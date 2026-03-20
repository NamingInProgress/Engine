package com.vke.core.assets.pipeline.protocols;

import com.vke.api.assets.Protocols;
import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.StageFilter;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.protocols.loader.UnsupportedLoader;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class PathProtocol implements AssetProtocol<Path> {
    @Override
    public String getProtocolName() {
        return Protocols.PATH;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetPipelineException {
        throw AssetPipelineException.unknownSelector(getProtocolName(), uri.getSelector());
    }

    @Override
    public Loader getLoader() {
        return new UnsupportedLoader(getProtocolName());
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        return applyForPathString(a.getDataAs(), b.getDataAs(), op);
    }

    private boolean applyForPathString(Path query, Path path, Op op) {
        //i cant be fucking asked
        String normalized1 = query.toString().replace('\\', '/');
        String normalized2 = path.toString().replace('\\', '/');
        return switch (op) {
            case EQUALS -> normalized2.equals(normalized1);
            case CONTAINS -> normalized2.contains(normalized1);
            case MATCHES -> Pattern.compile(normalized1).asMatchPredicate().test(normalized2);
        };
    }
}
