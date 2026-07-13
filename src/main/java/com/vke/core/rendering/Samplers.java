package com.vke.core.rendering;

import com.vke.api.rendering.abstraction.RenderResourceManager;
import com.vke.api.rendering.abstraction.RenderSystem;
import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.enums.Filter;

public class Samplers {

    public static Sampler NEAREST;
    public static Sampler LINEAR;

    public static void init(RenderSystem sys, RenderResourceManager resources) {
        NEAREST = sys.device().createSampler(new Sampler.Description(Filter.NEAREST, Filter.NEAREST));
        LINEAR = sys.device().createSampler(new Sampler.Description(Filter.LINEAR, Filter.LINEAR));

        resources.registerSampler("NEAREST", NEAREST);
        resources.registerSampler("LINEAR", LINEAR);
    }

    public static void free() {
        NEAREST.free();
        LINEAR.free();
    }
}
