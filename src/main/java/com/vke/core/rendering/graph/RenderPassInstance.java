package com.vke.core.rendering.graph;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.rendering.graph.def.RenderPassDefinition;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class RenderPassInstance {
    public final RenderPass executor;
    private final RenderPassDefinition def;

    private final HashMap<String, Texture> resolvedInputs = new HashMap<>();
    private final LinkedHashMap<String, Texture> resolvedOutputs = new LinkedHashMap<>();

    public RenderPassInstance(RenderSystem sys, RenderPassDefinition def) throws NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        this.def = def;
        this.executor = (RenderPass) def.clazz().getDeclaredConstructor(RenderSystem.class, RenderPassInstance.class)
                .newInstance(sys, this);
    }

    public void addInput(String localName, Texture texture) {
        this.resolvedInputs.put(localName, texture);
    }

    public void addOutput(String name, Texture texture) {
        this.resolvedOutputs.put(name, texture);
    }

    public Texture getInputTexture(String localName) {
        return resolvedInputs.get(localName);
    }

    public Texture getOutputTexture(String name) {
        return resolvedOutputs.get(name);
    }

    public RenderPassDefinition getDefinition() {
        return def;
    }

    public void clear() {
        this.resolvedInputs.clear();
        this.resolvedOutputs.clear();
    }
}
