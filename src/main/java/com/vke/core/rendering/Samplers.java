package com.vke.core.rendering;

import com.vke.api.rendering.abstraction.renderer.RenderDevice;
import com.vke.api.rendering.abstraction.renderer.RenderResourceManager;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.Sampler;
import com.vke.api.rendering.abstraction.renderer.enums.Filter;
import com.vke.api.rendering.abstraction.renderer.enums.SamplerAddressMode;

public class Samplers {

    public static Sampler NEAREST;
    public static Sampler LINEAR;

    public static void init(RenderSystem sys, RenderResourceManager resources) {
        RenderDevice dev = sys.device();
        NEAREST = dev.createSamplerManaged(new Sampler.Description(Filter.NEAREST, Filter.NEAREST));
        LINEAR = dev.createSamplerManaged(new Sampler.Description(Filter.LINEAR, Filter.LINEAR));

        resources.registerSampler("NEAREST", NEAREST);
        resources.registerSampler("LINEAR", LINEAR);
    }
}
