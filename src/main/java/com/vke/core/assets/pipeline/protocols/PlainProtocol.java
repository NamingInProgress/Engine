package com.vke.core.assets.pipeline.protocols;

import com.vke.api.assets.Protocols;
import com.vke.core.VKEngine;
import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;

import java.util.Objects;
import java.util.regex.Pattern;

public class PlainProtocol implements AssetProtocol<String> {
    @Override
    public String getProtocolName() {
        return Protocols.PLAIN;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetPipelineException {
        throw AssetPipelineException.unknownSelector(getProtocolName(), uri.getSelector());
    }

    @Override
    public Loader getLoader() {
        return new PlainProtocolLoader();
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        String sa = a.getDataAs();
        String sb = b.getDataAs();
        return switch (op) {
            case EQUALS -> Objects.equals(sa, sb);
            case CONTAINS -> safeContains(sa, sb);
            case MATCHES -> Pattern.compile(sb).asPredicate().test(sa);
        };
    }

    private boolean safeContains(String a, String b) {
        if (a == null) return false;
        return a.contains(b);
    }

    public static class PlainProtocolLoader implements Loader {
        @Override
        public AssetData load(VKEngine engine, Identifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetPipelineException {
            return AssetData.plain(Utils.chainExceptions(() -> Utils.readStringFromInputStream(identifier.asInputStream())));
        }
    }
}
