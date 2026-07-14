package com.vke.api.rendering.abstraction.rendergraph;

import java.util.HashMap;
import java.util.Objects;

public class RenderPassDefinition {

    private final String name;
    private final Class<?> clazz;
    private final HashMap<String, InputTextureDefinition> inputs;
    private final HashMap<String, TextureType> outputs;
    private final HashMap<String, String> uniformStuff;

    public RenderPassDefinition(String name, Class<?> clazz,
                                HashMap<String, InputTextureDefinition> inputs,
                                HashMap<String, TextureType> outputs,
                                HashMap<String, String> uniformStuff) {
        this.name = name;
        this.clazz = clazz;
        this.inputs = inputs;
        this.outputs = outputs;
        this.uniformStuff = uniformStuff;
    }

    public String name() {
        return name;
    }

    public Class<?> clazz() {
        return clazz;
    }

    public HashMap<String, InputTextureDefinition> inputs() {
        return inputs;
    }

    public HashMap<String, TextureType> outputs() {
        return outputs;
    }

    public HashMap<String, String> uniformStuff() {
        return uniformStuff;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RenderPassDefinition) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.clazz, that.clazz) &&
                Objects.equals(this.inputs, that.inputs) &&
                Objects.equals(this.outputs, that.outputs) &&
                Objects.equals(this.uniformStuff, that.uniformStuff);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, clazz, inputs, outputs, uniformStuff);
    }

    @Override
    public String toString() {
        return "RenderPassDefinition[" +
                "name=" + name + ", " +
                "clazz=" + clazz + ", " +
                "inputs=" + inputs + ", " +
                "outputs=" + outputs + ", " +
                "uniformStuff=" + uniformStuff + ']';
    }

    public record InputTextureDefinition(String source, String uniformFieldName, int width, int height, float scale) { }

    public enum TextureType {
        RENDER_TARGET("render-target"),
        COLOR("color"),
        DEPTH("depth"),
        STENCIL("stencil");

        public final String name;

        TextureType(String name) {
            this.name = name;
        }

        public static TextureType fromString(String name) {
            for (TextureType value : values()) {
                if (value.name.equalsIgnoreCase(name)) return value;
            }
            return COLOR;
        }
    }

}
