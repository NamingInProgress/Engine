package com.vke.core.assets.pipeline;

import com.vke.api.assets.Protocols;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.utils.io.FileUtils;
import com.vke.utils.io.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class StageElement {
    private final Path path;
    private AssetData data;
    private Identifier assetName;

    public StageElement(Path path, AssetData data) {
        this.path = path;
        this.data = data;
        this.assetName = new Identifier(path.getName(0).toString(), FileUtils.getFileName(path));
    }

    public StageElement(AssetData data) {
        this.path = null;
        this.data = data;
        this.assetName = null;
    }

    public Path getPath() {
        return path;
    }

    public AssetData getAssetData(@Nullable String protocol) {
        return AssetProtocol.getAssetData(this, protocol == null ? getProtocol() : protocol);
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
}
