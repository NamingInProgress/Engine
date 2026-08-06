package com.vke.api.rendering.abstraction.light;

import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldResource;

public interface LightManager {

    void write(FieldArrayResource buf, FieldResource count);

}
