package com.vke.core.assets.pipeline.protocols.loader;

import com.vke.api.assets.AssetMeta;
import com.vke.api.serializer.Serializer;
import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.assets.CacheHandler;
import com.vke.core.assets.meta.AssetMetaAttributes;
import com.vke.core.assets.meta.AttributedAssetMeta;
import com.vke.core.assets.pipeline.AssetPipelinePhase;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.core.serializer.impl.save.BinarySaver;
import com.vke.utils.Utils;
import com.vke.utils.io.FileUtils;

import java.nio.file.Files;
import java.util.HexFormat;

public class PipelinedLoader implements AssetProtocol.Loader {
    private final AssetPipelinePhase pipeline;
    private final AssetMeta meta;

    public PipelinedLoader(AssetPipelinePhase pipeline, AssetMeta meta) {
        this.pipeline = pipeline;
        this.meta = meta;
    }

    @Override
    public AssetData load(Context context, FileIdentifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException {
        String protocol = meta.getProtocol();
        AssetMetaAttributes attribs = new AssetMetaAttributes();
        if (meta instanceof AttributedAssetMeta attributed) {
            attribs = attributed.getAttributes();
        }
        StageElement element = new StageElement(identifier.getNamespace(), identifier.toPath(), meta.getBundleName(), new AssetData(protocol, identifier), attribs);
        element.setAssetName(meta.getAssetName().getPath());
        pipeline.execute(element, executionTarget);
        AssetData data = element.getAssetData();
        AssetProtocol<?> assetProtocol = pipeline.getContext().getProtocol(data.getProtocol());
        if (!data.isResolved()) {
            AssetData resolved = assetProtocol.getLoader().load(context, identifier, executionTarget);
            element.setData(resolved);
        }

        if (assetProtocol.isCacheable() && !identifier.isJavaEmbed()) {
            Utils.chainExceptions(() -> {
                byte[] hash = FileUtils.hash(identifier);

                FileIdentifier bundlePath = identifier.reconstructBundleName(meta.getBundleName());
                FileIdentifier cachePath = bundlePath.extend(HexFormat.of().formatHex(hash) + ".bin");

                if (!cachePath.existsFile()) {
                    Files.createDirectories(bundlePath.toPath());
                    BinarySaver saver = new BinarySaver(cachePath.openOutputStream());
                    CacheHandler.CacheBlob blob = new CacheHandler.CacheBlob(identifier, element.getAssetName(), hash, assetProtocol.getProtocolName());
                    Serializer.saveObject(blob, saver);
                    assetProtocol.serializeData(data.getDataAs(), saver);
                }

                return null;
            });
        }

        return element.getAssetData();
    }
}
