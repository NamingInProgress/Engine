package com.vke.api.assets.pipeline;

import com.vke.api.language.Str;
import com.vke.utils.FileUtils;
import com.vke.utils.Identifier;

import java.nio.file.Path;

public class StageElement {
    private final Path path;
    private String protocol;
    private Object data;
    private Identifier assetName;

    public StageElement(Path path, String protocol, Object data) {
        this.path = path;
        this.data = data;
        this.protocol = protocol;
        this.assetName = new Identifier(path.getName(0).toString(), FileUtils.getFileName(path));
    }

    public Path getPath() {
        return path;
    }

    public Object getData() {
        return data;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setData(String protocol, Object data) {
        this.data = data;
        this.protocol = protocol;
    }

    public Identifier getAssetName() {
        return assetName;
    }

    public void setAssetName(String newName) {
        assetName = new Identifier(assetName.getNamespace(), newName);
    }
}
