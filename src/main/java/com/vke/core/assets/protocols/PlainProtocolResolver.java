package com.vke.core.assets.protocols;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.StageFilter;
import com.vke.api.assets.pipeline.apis.ProtocolResolver;
import com.vke.core.assets.handles.PlainAssetHandle;
import com.vke.core.assets.handles.primitives.StringAssetHandle;
import com.vke.utils.Identifier;
import com.vke.utils.Utils;

import java.io.IOException;

public class PlainProtocolResolver implements ProtocolResolver<String> {
    @Override
    public boolean checkProtocolContent(StageFilter filter, StageElement stageElement) throws AssetPipelineException {
        return false;
    }

    @Override
    public AssetHandle<?> createHandle(StageElement element) throws AssetPipelineException {
        verifyProtocol("finishing", element.getProtocol(), "plain");
        Object data = element.getData();

        if (data instanceof Identifier identifier) {
            return new PlainAssetHandle(identifier);
        } else if (data instanceof String s) {
            return new StringAssetHandle(s);
        }

        throw new AssetPipelineException("Cannot create handle from StageElement");
    }

    @Override
    public String resolveData(StageElement element) throws AssetPipelineException {
        String protocol = element.getProtocol();
        Object data = element.getData();
        verifyProtocol("unknown", protocol, "plain");

        if (data instanceof Identifier identifier) {
            try {
                return Utils.readStringFromInputStream(identifier.asInputStream());
            } catch (IOException e) {
                throw new AssetPipelineException(e);
            }
        } else if (data instanceof String s) {
            return s;
        }

        throw new AssetPipelineException("Cannot create data from StageElement");
    }
}
