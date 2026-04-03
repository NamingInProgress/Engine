package com.vke.core.assets.pipeline.apis;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.rendering.abstraction.pipeline.GraphicsPipeline;
import com.vke.core.assets.language.Language;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.utils.io.Identifier;

import java.nio.file.Path;

import static com.vke.api.assets.Protocols.*;

public class AssetData {
    private final String protocol;
    private final Object resolvedData;
    private final Identifier unresolved;

    private AssetData(String protocol, Object resolvedData, Identifier unresolved) {
        this.protocol = protocol;
        this.resolvedData = resolvedData;
        this.unresolved = unresolved;
    }

    public AssetData(String protocol, Object resolvedData) {
        this.protocol = protocol;
        this.resolvedData = resolvedData;
        this.unresolved = null;
    }

    public AssetData(String protocol, Identifier unresolved) {
        this.protocol = protocol;
        this.unresolved = unresolved;
        this.resolvedData = null;
    }

    public AssetData reinterpret(String newProtocol) {
        return new AssetData(newProtocol, resolvedData, unresolved);
    }

    public String getProtocol() {
        return protocol;
    }

    public Object getData() {
        return resolvedData;
    }

    @SuppressWarnings("unchecked")
    public <T> T getDataAs() {
        return (T) resolvedData;
    }

    public Identifier getUnresolved() {
        return unresolved;
    }

    public boolean isResolved() {
        return resolvedData != null;
    }


    public static AssetData plain(String data) {
        return new AssetData(PLAIN, data);
    }

    public static AssetData plain(Identifier identifier) {
        return new AssetData(PLAIN, identifier);
    }

    public static AssetData bool(boolean data) {
        return new AssetData(PRIMITIVE_BOOL, data);
    }

    public static AssetData number(float data) {
        return new AssetData(PRIMITIVE_NUMBER, data);
    }

    public static AssetData config(ConfigDocument document) {
        return new AssetData(CONFIG, document);
    }

    public static AssetData config(Identifier identifier) {
        return new AssetData(CONFIG, identifier);
    }

    public static AssetData file(StageElement element) {
        return new AssetData(FILE, element);
    }

    public static AssetData path(Path path) {
        return new AssetData(PATH, path);
    }

    public static AssetData lang(Language language) {
        return new AssetData(LANG, language);
    }
    
    public static AssetData renderPipeline(GraphicsPipeline pl) {
        return new AssetData(RENDERPIPELINE, pl);
    }
}
