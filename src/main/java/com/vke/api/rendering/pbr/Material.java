package com.vke.api.rendering.pbr;

import com.vke.api.rendering.abstraction.renderer.data.TexturableEncoder;

import java.util.List;
import java.util.Objects;

public class Material {

    private final int flag;

    public BaseLayer base;

    public Material(List<MaterialLayer> layers) {
        for (MaterialLayer layer : layers) {
            switch (layer) {
                case BaseLayer b -> this.base = b;
                default -> {}
            }
        }

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

    public void putSelf(TexturableEncoder encoder) {
        encoder.uint1(flag);
        base.putSelf(encoder);
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
