package com.vke.api.assets.r;

import com.vke.api.assets.AssetHandle;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.api.rendering.abstraction.pipeline.ComputePipeline;
import com.vke.core.audio.source.AudioClip;
import com.vke.core.mesh.MeshPrefab;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.shader.Shader;
import com.vke.core.assets.language.Language;

public class R {
    public static Category<RenderPipeline> pipelines = new Category<>();
    public static Category<ComputePipeline> compute_pipelines = new Category<>();
    public static Category<Texture> textures = new Category<>();
    public static Category<Boolean> booleans = new Category<>();
    public static Category<Float> numbers = new Category<>();
    public static Category<String> strings = new Category<>();
    public static Category<ConfigDocument> configs = new Category<>();
    public static Category<Language> languages = new Category<>();
    public static Category<Shader> shaders = new Category<>();
    public static Category<MeshPrefab> meshprefabs = new Category<>();
    public static Category<AudioClip> audios = new Category<>();
    public static Category<ConfigSchema> schemas = new Category<>();
}
