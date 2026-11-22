package com.vke.core.scene.env;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.color.Color;
import com.vke.core.color.RgbColor;
import com.vke.utils.io.Disposable;

import java.io.IOException;

public class SceneEnvironment implements Disposable {
    private final Context context;
    private RgbColor clearColor;
    private EnvironmentMap environmentMap;

    public SceneEnvironment(Context context) {
        this.context = context;
    }

    public void setClearColor(Color clearColor) {
        this.clearColor = clearColor.toRgb();
    }

    public void setEnvironmentMap(AssetHandle<Texture> map) throws IOException {
        if (environmentMap != null) {
            environmentMap.free();
        }
        this.environmentMap = EnvironmentMap.ofAssetHandle(context, map);
    }

    public void setEnvironmentMap(Texture map) {
        setEnvironmentMap(map, EnvironmentMap.MapType.SPHERICAL);
    }

    public void setEnvironmentMap(Texture map, EnvironmentMap.MapType mapType) {
        if (environmentMap != null) {
            environmentMap.free();
        }
        this.environmentMap = new EnvironmentMap(context, map, mapType);
    }

    public void setEnvironmentMap(Identifier map) throws IOException {
        setEnvironmentMap(R.textures.get(map));
    }

    public void setEnvironmentMap(String map) throws IOException {
        setEnvironmentMap(R.textures.get(map));
    }

    public EnvironmentMap getEnvironmentMap() {
        return this.environmentMap;
    }

    @Override
    public void free() {
        if (environmentMap != null) {
            environmentMap.free();
        }
    }
}
