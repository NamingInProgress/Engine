package com.vke.api.vulkan.pipeline;

import com.vke.core.rendering.vulkan.shader.Shader;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

public abstract class PushConstantsDefinition {

    public abstract int getSize();
    public abstract int getOffset();
    public abstract Shader.Stages getAplicableStages();
    public abstract ByteBuffer getBytes(MemoryStack stack);

}
