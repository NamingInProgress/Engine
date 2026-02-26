package com.vke.core.vulkan.sampler;

import com.vke.api.rendering.abstraction.RenderDevice;
import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.enums.Filter;

public class Samplers {

    public static Sampler NEAREST;
    public static Sampler LINEAR;

    public static void init(RenderDevice device) {
        NEAREST = device.createSampler(new Sampler.Description(Filter.NEAREST, Filter.NEAREST));
        LINEAR = device.createSampler(new Sampler.Description(Filter.LINEAR, Filter.LINEAR));
    }

}
