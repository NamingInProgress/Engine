package com.vke.core.rendering.pbr.deserialize;

import com.vke.api.assets.r.R;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.rendering.pbr.BaseLayer;
import com.vke.core.Context;
import com.vke.core.color.Color;
import com.vke.core.color.RgbColor;

public class BaseMaterialLayerDeserializer extends MaterialLayerDeserializer<BaseLayer> {

    @Override
    public BaseLayer accept(Context context, ConfigNode node) {
        RgbColor color = node.getStringOption("base-albedo").map(c -> Color.parse(context, c).toRgb()).unwrapOrIdentity();
        var albedo = node.getStringOption("albedo").map(R.textures::get).unwrapOr(BaseLayer.ALBEDO);
        var normal = node.getStringOption("normal").map(R.textures::get).unwrapOr(BaseLayer.NORMAL);
        var mr = node.getStringOption("metallic-roughness").map(R.textures::get).unwrapOr(BaseLayer.METALLIC_ROUGHNESS);
        var oc = node.getStringOption("occlusion").map(R.textures::get).unwrapOr(BaseLayer.OCCLUSION);
        var emissive = node.getStringOption("emissive").map(R.textures::get).unwrapOr(BaseLayer.EMISSIVE);
        var specular = node.getStringOption("specular").map(R.textures::get).unwrapOr(BaseLayer.SPECULAR);

        float normalScale = node.getNumberOption("normal-scale").unwrapOrIdentity();
        float metallic = node.getNumberOption("metallic-scale").unwrapOrIdentity();
        float roughness = node.getNumberOption("roughness-scale").unwrapOrIdentity();
        float occlusion = node.getNumberOption("occlusion-scale").unwrapOrIdentity();
        RgbColor emissiveColor = node.getStringOption("emissive-color").map(c -> Color.parse(context, c).toRgb()).unwrapOrIdentity();
        float emissiveScale = node.getNumberOption("emissive-scale").unwrapOrIdentity();
        float specularScale = node.getNumberOption("specular-scale").unwrapOrIdentity();

        return new BaseLayer(color, albedo, normal, mr, oc, emissive, specular,
                normalScale, metallic, roughness, occlusion, emissiveColor, emissiveScale, specularScale);
    }
}
