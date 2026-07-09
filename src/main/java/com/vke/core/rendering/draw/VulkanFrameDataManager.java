package com.vke.core.rendering.draw;

import com.vke.api.rendering.abstraction.data.IFrameDataManager;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.core.vulkan.descriptor.EngineDescriptorSetsManager;

public class VulkanFrameDataManager implements IFrameDataManager {

    private final EngineDescriptorSetsManager mgr;

    private final BufferHandle handle;

    public VulkanFrameDataManager(EngineDescriptorSetsManager mgr) {
        this.mgr = mgr;
        this.handle = mgr.ENGINE_PIPELINE_LAYOUT.getGroup().resolve("camera");
    }

}
