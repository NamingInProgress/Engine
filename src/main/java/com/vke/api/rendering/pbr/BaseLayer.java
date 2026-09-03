package com.vke.api.rendering.pbr;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.core.assets.handles.LazyAssetHandle;
import com.vke.core.color.RgbColor;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;

import java.io.IOException;
import java.util.Objects;

public class BaseLayer extends MaterialLayer {

    public static final BaseLayer DEFAULT = new BaseLayer();

    public static final LazyAssetHandle<Texture> ALBEDO = R.textures.get("vke:textures/material/defaults/default_albedo.png");
    public static final LazyAssetHandle<Texture> NORMAL = R.textures.get("vke:textures/material/defaults/default_normal.png");
    public static final LazyAssetHandle<Texture> METALLIC_ROUGHNESS = R.textures.get("vke:textures/material/defaults/default_metallic_roughness.png");
    public static final LazyAssetHandle<Texture> OCCLUSION = R.textures.get("vke:textures/material/defaults/default_occlusion.png");
    public static final LazyAssetHandle<Texture> EMISSIVE = R.textures.get("vke:textures/material/defaults/default_emissive.png");
    public static final LazyAssetHandle<Texture> SPECULAR = R.textures.get("vke:textures/material/defaults/default_specular.png");

    public final RgbColor baseAlbedo;
    public final AssetHandle<Texture> albedo;
    public final AssetHandle<Texture> normal;
    public final AssetHandle<Texture> metallicRoughness;
    public final AssetHandle<Texture> occlusionTexture;
    public final AssetHandle<Texture> emissive;
    public final AssetHandle<Texture> specular;

    public final float normalScale;

    public final float metallic;
    public final float roughness;

    public final float occlusionStrength;

    public final RgbColor emissiveColor;
    public final float emissiveStrength;

    public final float specularScale;

    private BaseLayer() {
        this(RgbColor.WHITE, ALBEDO, NORMAL, METALLIC_ROUGHNESS, OCCLUSION, EMISSIVE, SPECULAR);
    }

    public BaseLayer(BaseLayer copy) {
        this(copy.baseAlbedo, copy.albedo, copy.normal, copy.metallicRoughness, copy.occlusionTexture, copy.emissive, copy.specular,
                copy.normalScale, copy.metallic, copy.roughness, copy.occlusionStrength, copy.emissiveColor, copy.emissiveStrength, copy.specularScale);
    }

    public BaseLayer(RgbColor baseAlbedo, AssetHandle<Texture> albedo, AssetHandle<Texture> normal,
                     AssetHandle<Texture> metallicRoughness, AssetHandle<Texture> occlusionTexture, AssetHandle<Texture> emissive,
                     AssetHandle<Texture> specular) {
        this(baseAlbedo, albedo, normal, metallicRoughness, occlusionTexture, emissive, specular, 1.0f, 1.0f,
                1.0f, 1.0f, RgbColor.BLACK, 0.0f, 1.0f);
    }

    public BaseLayer(RgbColor baseAlbedo, AssetHandle<Texture> albedo, AssetHandle<Texture> normal,
                     AssetHandle<Texture> metallicRoughness, AssetHandle<Texture> occlusionTexture, AssetHandle<Texture> emissive,
                     AssetHandle<Texture> specular,
                     float normalScale, float metallic, float roughness, float occlusionStrength, RgbColor emissiveColor, float emissiveStrength,
                     float specularScale) {
        this.baseAlbedo = baseAlbedo;
        this.albedo = albedo;
        this.normal = normal;
        this.metallicRoughness = metallicRoughness;
        this.occlusionTexture = occlusionTexture;
        this.emissive = emissive;
        this.specular = specular;
        this.normalScale = normalScale;
        this.metallic = metallic;
        this.roughness = roughness;
        this.occlusionStrength = occlusionStrength;
        this.emissiveColor = emissiveColor;
        this.emissiveStrength = emissiveStrength;
        this.specularScale = specularScale;
    }

    @Override
    public void putSelf(RenderSystem sys, BufferSlice encoder) throws IOException {
        encoder.sampler2D(sys, albedo.acquire(sys));
        encoder.sampler2D(sys, normal.acquire(sys));
        encoder.sampler2D(sys, metallicRoughness.acquire(sys));
        encoder.sampler2D(sys, occlusionTexture.acquire(sys));
        encoder.sampler2D(sys, emissive.acquire(sys));
        encoder.sampler2D(sys, specular.acquire(sys));

        encoder.float1(normalScale);

        baseAlbedo.putSelf(encoder);

        encoder.float1(metallic);
        encoder.float1(roughness);
        encoder.float1(occlusionStrength);

        encoder.float1(emissiveStrength);
        emissiveColor.putSelf(encoder);

        encoder.float1(specularScale);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BaseLayer baseLayer = (BaseLayer) o;
        return Float.compare(normalScale, baseLayer.normalScale) == 0 && Float.compare(metallic, baseLayer.metallic) == 0 && Float.compare(roughness, baseLayer.roughness) == 0 && Float.compare(occlusionStrength, baseLayer.occlusionStrength) == 0 && Float.compare(emissiveStrength, baseLayer.emissiveStrength) == 0 && Float.compare(specularScale, baseLayer.specularScale) == 0 && Objects.equals(baseAlbedo, baseLayer.baseAlbedo) && Objects.equals(albedo, baseLayer.albedo) && Objects.equals(normal, baseLayer.normal) && Objects.equals(metallicRoughness, baseLayer.metallicRoughness) && Objects.equals(occlusionTexture, baseLayer.occlusionTexture) && Objects.equals(emissive, baseLayer.emissive) && Objects.equals(specular, baseLayer.specular) && Objects.equals(emissiveColor, baseLayer.emissiveColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseAlbedo, albedo, normal, metallicRoughness, occlusionTexture, emissive, specular, normalScale, metallic, roughness, occlusionStrength, emissiveColor, emissiveStrength, specularScale);
    }
}
