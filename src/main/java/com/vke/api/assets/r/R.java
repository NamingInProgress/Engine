package com.vke.api.assets.r;

import com.vke.api.abstraction.data.Texture;
import com.vke.api.assets.AssetHandle;
import com.vke.api.vulkan.pipeline.RenderPipeline;
import com.vke.utils.Identifier;

import java.io.IOException;
import java.lang.reflect.Field;

public class R {
    public static Category<RenderPipeline> pipelines = new Category<>();
    public static Category<Texture> textures = new Category<>();
    public static Category<Boolean> booleans = new Category<>();
    public static Category<Float> floats = new Category<>();
    public static Category<String> strings = new Category<>();

    /**
     * Examples:
     * shaders/myshader.frag
     * textures/smiley.png
     * @param rQuery
     * @return
     * @param <T>
     */
    public static <T> AssetHandle<T> findFancyByNameByTheCostOfIncreasedRuntimeCost(String rQuery) throws IOException {
        String[] parts = rQuery.split("/", 2);
        String cat = parts[0];
        String asset = parts[1];

        try {
            Field field = R.class.getDeclaredField(cat);
            Category<T> category = (Category<T>) field.get(null);
            return category.get(Identifier.of(asset));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
