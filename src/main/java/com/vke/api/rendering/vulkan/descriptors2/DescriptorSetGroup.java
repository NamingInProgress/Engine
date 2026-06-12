package com.vke.api.rendering.vulkan.descriptors2;

import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;

import java.util.HashMap;

public class DescriptorSetGroup {

    private final HashMap<String, UniformHandle> handleCache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends UniformHandle> T resolve(String path) {
        if (handleCache.containsKey(path)) return (T) handleCache.get(path);


    }

}
