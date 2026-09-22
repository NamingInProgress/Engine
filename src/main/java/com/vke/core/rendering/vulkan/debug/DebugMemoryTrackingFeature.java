package com.vke.core.rendering.vulkan.debug;

import com.vke.api.rendering.abstraction.debug.DebugFeature;

public class DebugMemoryTrackingFeature extends DebugFeature {

    protected DebugMemoryTrackingFeature() {
        super(DebugFeatures.MEMORY_TRACKER_FEATURE_NAME);
    }

    @Override
    public void free() {

    }
}
