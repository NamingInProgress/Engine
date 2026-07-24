package com.vke.core.rendering.pbr.deserialize;

import com.vke.api.assets.r.R;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.rendering.pbr.BaseLayer;
import com.vke.core.color.Color;

public class BaseMaterialLayerDeserializer extends MaterialLayerDeserializer<BaseLayer> {

    private BaseLayer getBaseLayer(ConfigNode node) {
        Color color = Color.parse(node.getString("base-albedo"));
        var albedo = node.getStringOption("albedo").map(R.textures::get).unwrapOr(BaseLayer.ALBEDO);
        var normal = node.getStringOption("normal").map(R.textures::get).unwrapOr(BaseLayer.NORMAL);
        var mr = node.getStringOption("metallic-roughness").map(R.textures::get).unwrapOr(BaseLayer.METALLIC_ROUGHNESS);
        var oc = node.getStringOption("occlusion").map(R.textures::get).unwrapOr(BaseLayer.OCCLUSION);
        var emissive = node.getStringOption("emissive").map(R.textures::get).unwrapOr(BaseLayer.EMISSIVE);

        float normalScale = node.getNumberOption("normal-scale").unwrapOrIdentity();
        float metallic = node.getNumberOption("metallic-scale").unwrapOrIdentity();
        float roughness = node.getNumberOption("roughness-scale").unwrapOrIdentity();
        float occlusion = node.getNumberOption("occlusion-scale").unwrapOrIdentity();
        Color emissiveColor = Color.parse(node.getString("emissive-color"));
        float emissiveScale = node.getNumberOption("emissive-scale").unwrapOrIdentity();

        return new BaseLayer(color, albedo, normal, mr, oc, emissive, normalScale, metallic, roughness, occlusion, emissiveColor, emissiveScale);
    }

    @Override
    public BaseLayer accept(ConfigNode node) {
        return getBaseLayer(node);
    }
}
