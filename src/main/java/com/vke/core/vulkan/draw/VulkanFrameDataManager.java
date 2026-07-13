package com.vke.core.vulkan.draw;

import com.vke.api.rendering.abstraction.data.FrameDataManager;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.core.vulkan.descriptor.EngineDescriptorSetsManager;

public class VulkanFrameDataManager implements FrameDataManager {

    private final EngineDescriptorSetsManager mgr;

    private final BufferHandle handle;

    public VulkanFrameDataManager(EngineDescriptorSetsManager mgr) {
        this.mgr = mgr;
        this.handle = mgr.ENGINE_PIPELINE_LAYOUT.getGroup().resolve("camera");
    }

}
