package com.vke.core.rendering.graph.def;

import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class RenderPassDefinition {

    private final String name;
    private final Class<?> clazz;
    private final List<InputTextureDefinition> inputs;
    private final List<OutputTextureDefinition> outputs;

    public RenderPassDefinition(String name, Class<?> clazz,
                                List<InputTextureDefinition> inputs,
                                List<OutputTextureDefinition> outputs) {
        this.name = name;
        this.clazz = clazz;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public String name() {
        return name;
    }

    public Class<?> clazz() {
        return clazz;
    }

    public List<InputTextureDefinition> inputs() {
        return inputs;
    }

    public List<OutputTextureDefinition> outputs() {
        return outputs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RenderPassDefinition) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.clazz, that.clazz) &&
                Objects.equals(this.inputs, that.inputs) &&
                Objects.equals(this.outputs, that.outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, clazz, inputs, outputs);
    }

    @Override
    public String toString() {
        return "RenderPassDefinition[" +
                "name=" + name + ", " +
                "clazz=" + clazz + ", " +
                "inputs=" + inputs + ", " +
                "outputs=" + outputs + ']';
    }

    public record InputTextureDefinition(String localName, String source, String uniformFieldName) { }
    public record OutputTextureDefinition(String name, String source, TextureType type, Format format, int width, int height, float scale) {}

    public enum TextureType {
        RENDER_TARGET("render-target"),
        COLOR("color"),
        DEPTH("depth"),
        STENCIL("stencil"),
        STORAGE("storage"),
        DEPTH_STENCIL("depth_stencil");

        public final String name;

        TextureType(String name) {
            this.name = name;
        }

        public static TextureType fromString(String name) {
            try {
                return TextureType.valueOf(name);
            } catch (IllegalArgumentException e) {}
            for (TextureType value : values()) {
                if (value.name.equalsIgnoreCase(name)) return value;
            }
            return COLOR;
        }
    }

}
