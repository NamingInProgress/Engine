package com.vke.core.assets.pipeline.protocols;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Protocol;
import com.vke.api.font.FontFace;
import com.vke.api.vkz.ArchiveType;
import com.vke.api.vkz.VkzArchive;
import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.core.services2.Services;
import com.vke.core.vkz.service.Vkz;

@Protocol
public class VkzProtocol implements AssetProtocol<VkzArchive> {
    @Override
    public String getProtocolName() {
        return Protocols.VKZ;
    }

    @Override
    public AssetData getField(StageElement data, AssetUri uri) throws AssetException {
        return null;
    }

    @Override
    public Loader getLoader() {
        return new VkzProtocolLoader();
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        return false;
    }

    public static class VkzProtocolLoader implements Loader {
        @Override
        public AssetData load(Context context, FileIdentifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException {
            Vkz vkz = context.service(Services.VKZ);
            VkzArchive arch = vkz.open(identifier, ArchiveType.InflateAll);
            return AssetData.vkz(arch);
        }
    }

}
