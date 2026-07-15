package com.vke.api.rendering.abstraction.rendergraph.def;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class PostRenderPassDefinition extends RenderPassDefinition {

    private final ArrayList<PostStage> stages;

    public PostRenderPassDefinition(String name, Class<?> clazz,
                                    List<InputTextureDefinition> inputs,
                                    List<OutputTextureDefinition> outputs,
                                    HashMap<String, String> uniformStuff,
                                    ArrayList<PostStage> stages) {
        super(name, clazz, inputs, outputs, uniformStuff);
        this.stages = stages;
    }

    public enum PostStage {
        SSAO("ssao"),
        BLUR("blur");

        public final String tagName;

        PostStage(String tagName) {
            this.tagName = tagName;
        }

        public static PostStage fromString(String string) {
            for (PostStage value : values()) {
                if (value.tagName.equalsIgnoreCase(string)) return value;
            }
            throw new IllegalArgumentException("Could not find post stage for name: " + string);
        }

    }

}
