package com.vke.core.assets.pipeline.protocols;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Protocol;
import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.utils.Utils;

import java.util.Objects;
import java.util.regex.Pattern;

@Protocol
public class PlainProtocol implements AssetProtocol<String> {
    @Override
    public String getProtocolName() {
        return Protocols.PLAIN;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetException {
        throw AssetException.unknownSelector(getProtocolName(), uri.getSelector());
    }

    @Override
    public Loader getLoader() {
        return new PlainProtocolLoader();
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        String sa = a.getDataAs();
        String sb = b.getDataAs();
        if (sa == null && sb == null) return true;
        if (sa == null || sb == null) return false;
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
        public AssetData load(Context context, FileIdentifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException {
            return AssetData.plain(Utils.chainExceptions(() -> Utils.readStringFromInputStream(identifier.openInputStream())));
        }
    }
}
