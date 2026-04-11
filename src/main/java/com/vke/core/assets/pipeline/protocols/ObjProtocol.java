package com.vke.core.assets.pipeline.protocols;

import com.vke.api.assets.Protocols;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.core.file.obj.ObjFile;
import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;

public class ObjProtocol implements AssetProtocol<ObjFile> {
    @Override
    public String getProtocolName() {
        return Protocols.OBJ;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetException {
        throw AssetException.unknownSelector(getProtocolName(), uri.getSelector());
    }

    @Override
    public Loader getLoader() {
        return null;
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        return false;
    }

    public static class ObjLoader implements Loader {
        @Override
        public AssetData load(Context context, Identifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException {
            return Utils.chainExceptions(() -> new AssetData(Protocols.OBJ, new ObjFile(identifier.asInputStream())));
        }
    }
}
