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
    public static Sampler U_REPEAT_V_CLAMP_LINEAR;
    public static Sampler U_REPEAT_V_CLAMP_NEAREST;

    public static void init(RenderSystem sys, RenderResourceManager resources) {
        RenderDevice dev = sys.device();
        NEAREST = dev.createSamplerManaged(new Sampler.Description(Filter.NEAREST, Filter.NEAREST));
        LINEAR = dev.createSamplerManaged(new Sampler.Description(Filter.LINEAR, Filter.LINEAR));
        U_REPEAT_V_CLAMP_LINEAR = dev.createSamplerManaged(new Sampler.Description(Filter.LINEAR, Filter.LINEAR, null, SamplerAddressMode.REPEAT, SamplerAddressMode.CLAMP_TO_EDGE));
        U_REPEAT_V_CLAMP_NEAREST = dev.createSamplerManaged(new Sampler.Description(Filter.NEAREST, Filter.NEAREST, null, SamplerAddressMode.REPEAT, SamplerAddressMode.CLAMP_TO_EDGE));

        resources.registerSampler("NEAREST", NEAREST);
        resources.registerSampler("LINEAR", LINEAR);
        resources.registerSampler("U_REPEAT_V_CLAMP_LINEAR", U_REPEAT_V_CLAMP_LINEAR);
        resources.registerSampler("U_REPEAT_V_CLAMP_NEAREST", U_REPEAT_V_CLAMP_NEAREST);
    }
}
