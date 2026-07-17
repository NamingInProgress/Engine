package com.vke.core.rendering.vulkan.data;

import com.vke.api.rendering.abstraction.renderer.RenderResourceManager;
import com.vke.api.rendering.abstraction.renderer.data.Sampler;
import com.vke.api.rendering.abstraction.renderer.data.StaticMesh;
import com.vke.core.mesh.Mesh;
import com.vke.core.rendering.vulkan.buffers.premade.mesh.VulkanStaticMesh;
import com.vke.core.rendering.Samplers;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.utils.io.Disposable;

import java.util.ArrayList;
import java.util.HashMap;

public class VulkanResourceManager implements RenderResourceManager {

    private final VulkanRenderSystem sys;
    private final HashMap<String, Sampler> samplers = new HashMap<>();
    private final ArrayList<Disposable> toFree = new ArrayList<>();

    public VulkanResourceManager(VulkanRenderSystem sys) {
        this.sys = sys;
        Samplers.init(sys, this);
    }

    @Override
    public StaticMesh uploadStaticMesh(Mesh<?> mesh) {
        VulkanStaticMesh sm = VulkanStaticMesh.uploadOnce(sys, mesh);
        toFree.add(sm);
        return sm;
    }

    @Override
    public Sampler samplerFromStringOrDefault(String samplerName) {
        return samplers.getOrDefault(samplerName, Samplers.LINEAR);
    }

    @Override
    public void registerSampler(String name, Sampler sampler) {
        this.samplers.put(name, sampler);
    }

    @Override
    public void free() {
        Samplers.free();
        toFree.forEach(Disposable::free);
    }
}
