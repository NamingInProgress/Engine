package com.vke.core.assets.pipeline;

import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.meta.AssetMetaAttributes;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.utils.io.FileUtils;
import com.vke.utils.io.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class StageElement {
    private final Path path;
    private AssetData data;
    private Identifier assetName;
    private final AssetMetaAttributes metaAttribs;
    private boolean wasProcessed;

    public StageElement(Path path, AssetData data, AssetMetaAttributes meta) {
        this.path = path;
        this.data = data;
        this.assetName = new Identifier(path.getName(0).toString(), FileUtils.getFileName(path));
        this.metaAttribs = meta;
    }

    public StageElement(AssetData data, AssetMetaAttributes meta) {
        this.path = null;
        this.data = data;
        this.assetName = null;
        this.metaAttribs = meta;
    }

    public Path getPath() {
        return path;
    }

    public AssetData getAssetData(@Nullable String protocol) {
        return AssetProtocol.getAssetData(this, protocol == null ? getProtocol() : protocol);
    }

    public AssetData getAssetDataResolved(Context context, AssetProtocol<?> protocol, PipelineStage.ExecutionTarget target) throws AssetException {
        AssetData d = AssetProtocol.getAssetData(this, protocol.getProtocolName());
        if (!d.isResolved() && target != PipelineStage.ExecutionTarget.Pseudo) {
            AssetProtocol.Loader loader = protocol.getLoader();
            d = loader.load(context, d.getUnresolved(), target);
        }
        return d;
    }

    public AssetData getAssetData() {
        return data;
    }

    public String getProtocol() {
        return data.getProtocol();
    }

    public void setData(String protocol, Object data) {
        this.data = new AssetData(protocol, data);
    }

    public void setData(AssetData data) {
        this.data = data;
    }

    public Identifier getAssetName() {
        return assetName;
    }

    public void setAssetName(String newName) {
        assetName = new Identifier(assetName.getNamespace(), newName);
    }

    public AssetMetaAttributes getMetaAttributes() {
        return metaAttribs;
    }

    public boolean wasProcessed() {
        return wasProcessed;
    }

    public void setProcessed() {
        wasProcessed = true;
    }
}
