package com.vke.api.rendering.abstraction.rendergraph;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.rendergraph.def.RenderPassDefinition;
import com.vke.utils.tuple.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class RenderPassInstance {
    public final RenderPass executor;
    private final RenderPassDefinition def;

    private HashMap<String, Texture> resolvedInputs = new HashMap<>();
    private LinkedHashMap<String, Pair<Texture, RenderPassDefinition.TextureType>> resolvedOutputs = new LinkedHashMap<>();

    public RenderPassInstance(RenderSystem sys, RenderPassDefinition def) throws NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        this.def = def;
        this.executor = (RenderPass) def.clazz().getDeclaredConstructor(RenderSystem.class, RenderPassInstance.class)
                .newInstance(sys, this);
    }

    public void addInput(String localName, Texture texture) {
        this.resolvedInputs.put(localName, texture);
    }

    public void addOutput(String name, Texture texture, RenderPassDefinition.TextureType type) {
        this.resolvedOutputs.put(name, new Pair<>(texture, type));
    }

    public Texture getInputTexture(String localName) {
        return resolvedInputs.get(localName);
    }

    public Texture getOutputTexture(String name) {
        return resolvedOutputs.get(name).v1;
    }

    public RenderPassDefinition getDefinition() {
        return def;
    }
}
