package com.vke.api.assets.r;

import com.vke.api.abstraction.data.Texture;
import com.vke.api.assets.AssetHandle;
import com.vke.api.vulkan.pipeline.RenderPipeline;

public class R {
    public static Category<RenderPipeline> pipelines = new Category<>();
    public static Category<Texture> textures = new Category<>();
    public static Category<Boolean> booleans = new Category<>();
    public static Category<Float> floats = new Category<>();
    public static Category<String> strings = new Category<>();
}
