package com.vke.api.rendering.pbr;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.RenderingEncoder;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Material {

    private final RenderSystem sys;

    private final int flag;

    public BaseLayer base;

    public Material(RenderSystem sys, List<MaterialLayer> layers) {
        this.sys = sys;

        for (MaterialLayer layer : layers) {
            switch (layer) {
                case BaseLayer b -> this.base = b;
                default -> {}
            }
        }

        if (this.base == null) throw new IllegalStateException("Missing base layer for material!");

        this.flag = computeFlag();
    }

    private int computeFlag() {
        int flag = 0;
        List<MaterialLayer> extLayers = List.of(); // everything except base layer but we dont have none of that yet lmao
        for (int i = 0; i < extLayers.size(); i++) {
            if (extLayers.get(i) != null) {
                flag |= (1 << i);
            }
        }

        return flag;
    }

    public void putSelf(BufferSlice encoder) {
        encoder.putInt(flag);
        try {
            base.putSelf(sys, encoder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Material material = (Material) o;
        return flag == material.flag && Objects.equals(base, material.base);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flag, base);
    }
}
