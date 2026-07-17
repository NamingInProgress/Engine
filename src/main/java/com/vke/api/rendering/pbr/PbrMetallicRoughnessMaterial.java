package com.vke.api.rendering.pbr;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.core.color.Color;

public class PbrMetallicRoughnessMaterial {
    private AssetHandle<Texture> baseColorTexture;
    private Color baseColorFactor;
    private AssetHandle<Texture> normalMap;
    private AssetHandle<Texture> specularMap;
    private AssetHandle<Texture> occlusionMap;
}
