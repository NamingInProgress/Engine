package com.vke.core.assets.pipeline.protocols.loader;

import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.stages.PipelineStage;

public class UnsupportedLoader implements AssetProtocol.Loader {
    private final String errorMessage;

    public UnsupportedLoader(String protocol) {
        this.errorMessage = String.format("The protocol %s cannot be loaded! Maybe use the <decode> stage instead?", protocol);
    }

    @Override
    public AssetData load(Context context, FileIdentifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException {
        throw new AssetException(errorMessage);
    }
}
