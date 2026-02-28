package com.vke.api.assets.pipeline;

import java.nio.file.Path;

public class StageElement {
    private final Path path;
    private String protocol;
    private Object data;

    public StageElement(Path path, String protocol, Object data) {
        this.path = path;
        this.data = data;
        this.protocol = protocol;
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
}
