package com.vke.api.rendering.pbr;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.data.TexturableEncoder;
import com.vke.core.assets.handles.LazyAssetHandle;
import com.vke.core.color.Color;

import java.util.Objects;

public class BaseLayer extends MaterialLayer {

    public static final BaseLayer DEFAULT = new BaseLayer();

    public static final LazyAssetHandle<Texture> ALBEDO = R.textures.get("vke:textures/material/defaults/default_albedo.png");
    public static final LazyAssetHandle<Texture> NORMAL = R.textures.get("vke:textures/material/defaults/default_normal.png");
    public static final LazyAssetHandle<Texture> METALLIC_ROUGHNESS = R.textures.get("vke:textures/material/defaults/default_metalling_roughness.png");
    public static final LazyAssetHandle<Texture> OCCLUSION = R.textures.get("vke:textures/material/defaults/default_occlusion.png");
    public static final LazyAssetHandle<Texture> EMISSIVE = R.textures.get("vke:textures/material/defaults/default_emissive.png");

    public final Color baseAlbedo;
    public final AssetHandle<Texture> albedo;
    public final AssetHandle<Texture> normal;
    public final AssetHandle<Texture> metallicRoughness;
    public final AssetHandle<Texture> occlusionTexture;
    public final AssetHandle<Texture> emissive;

    public final float normalScale;

    public final float metallic;
    public final float roughness;

    public final float occlusionStrength;

    public final Color emissiveColor;
    public final float emissiveStrength;

    private BaseLayer() {
        this(Color.WHITE, ALBEDO, NORMAL, METALLIC_ROUGHNESS, OCCLUSION, EMISSIVE);
    }

    public BaseLayer(BaseLayer copy) {
        this(copy.baseAlbedo, copy.albedo, copy.normal, copy.metallicRoughness, copy.occlusionTexture, copy.emissive,
                copy.normalScale, copy.metallic, copy.roughness, copy.occlusionStrength, copy.emissiveColor, copy.emissiveStrength);
    }

    public BaseLayer(Color baseAlbedo, AssetHandle<Texture> albedo, AssetHandle<Texture> normal,
                        AssetHandle<Texture> metallicRoughness, AssetHandle<Texture> occlusionTexture, AssetHandle<Texture> emissive) {
        this(baseAlbedo, albedo, normal, metallicRoughness, occlusionTexture, emissive, 1.0f, 1.0f,
                1.0f, 1.0f, Color.BLACK, 1.0f);
    }

    public BaseLayer(Color baseAlbedo, AssetHandle<Texture> albedo, AssetHandle<Texture> normal,
                        AssetHandle<Texture> metallicRoughness, AssetHandle<Texture> occlusionTexture, AssetHandle<Texture> emissive,
                        float normalScale, float metallic, float roughness, float occlusionStrength, Color emissiveColor, float emissiveStrength) {
        this.baseAlbedo = baseAlbedo;
        this.albedo = albedo;
        this.normal = normal;
        this.metallicRoughness = metallicRoughness;
        this.occlusionTexture = occlusionTexture;
        this.emissive = emissive;
        this.normalScale = normalScale;
        this.metallic = metallic;
        this.roughness = roughness;
        this.occlusionStrength = occlusionStrength;
        this.emissiveColor = emissiveColor;
        this.emissiveStrength = emissiveStrength;
    }

    @Override
    public void putSelf(TexturableEncoder encoder) {
        baseAlbedo.putSelf(encoder);
        encoder.sampler2D(albedo.get());
        encoder.sampler2D(normal.get());
        encoder.sampler2D(metallicRoughness.get());
        encoder.sampler2D(occlusionTexture.get());
        encoder.sampler2D(emissive.get());

        encoder.float1(normalScale);
        encoder.float1(metallic);
        encoder.float1(roughness);
        encoder.float1(occlusionStrength);

        emissiveColor.putSelf(encoder);
        encoder.float1(emissiveStrength);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BaseLayer baseLayer = (BaseLayer) o;
        return Float.compare(normalScale, baseLayer.normalScale) == 0 && Float.compare(metallic, baseLayer.metallic) == 0 && Float.compare(roughness, baseLayer.roughness) == 0 && Float.compare(occlusionStrength, baseLayer.occlusionStrength) == 0 && Float.compare(emissiveStrength, baseLayer.emissiveStrength) == 0 && Objects.equals(baseAlbedo, baseLayer.baseAlbedo) && Objects.equals(albedo, baseLayer.albedo) && Objects.equals(normal, baseLayer.normal) && Objects.equals(metallicRoughness, baseLayer.metallicRoughness) && Objects.equals(occlusionTexture, baseLayer.occlusionTexture) && Objects.equals(emissive, baseLayer.emissive) && Objects.equals(emissiveColor, baseLayer.emissiveColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseAlbedo, albedo, normal, metallicRoughness, occlusionTexture, emissive, normalScale, metallic, roughness, occlusionStrength, emissiveColor, emissiveStrength);
    }
}
