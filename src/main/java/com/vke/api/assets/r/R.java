package com.vke.api.assets.r;

import com.vke.api.abstraction.data.Texture;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.vulkan.pipeline.RenderPipeline;
import com.vke.core.assets.language.Language;

public class R {
    public static Category<RenderPipeline> pipelines = new Category<>();
    public static Category<Texture> textures = new Category<>();
    public static Category<Boolean> booleans = new Category<>();
    public static Category<Float> numbers = new Category<>();
    public static Category<String> strings = new Category<>();
    public static Category<ConfigDocument> configs = new Category<>();
    public static Category<Language> languages = new Category<>();
}
