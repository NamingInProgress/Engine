package com.vke.api.assets.pipeline.apis;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.StageFilter;
import com.vke.utils.Utils;

import java.net.URI;
import java.util.Arrays;

public interface ProtocolResolver<T> {
    boolean checkProtocolContent(StageFilter filter, StageElement stageElement) throws AssetPipelineException;
    String resolveUri(URI uri, StageElement stageElement) throws AssetPipelineException;
    AssetHandle<?> createHandle(StageElement element) throws AssetPipelineException;
    T resolveData(StageElement element) throws AssetPipelineException;

    default void verifyProtocol(String stage, String found, String... supported) throws AssetPipelineException {
        if (!Utils.arrayContains(supported, found)) {
            throw AssetPipelineException.incompatibleProtocol(stage, Arrays.toString(supported), found);
        }
    }
}
