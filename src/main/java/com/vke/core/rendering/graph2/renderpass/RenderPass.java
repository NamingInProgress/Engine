package com.vke.core.rendering.graph2.renderpass;

import com.vke.api.logger.Logger;
import com.vke.api.parsing.config.Configs;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.core.Identifier;
import com.vke.core.VKEngine;
import com.vke.core.color.RgbColor;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.rendering.graph2.GraphTexture;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.graph2.TextureCategory;
import com.vke.utils.io.SegmentedPath;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class RenderPass {
    public static final String IMAGE_TO_SCREEN_KEY = "image-to-screen";
    public static final Identifier IMAGE_TO_SCREEN_IDENT = new Identifier(VKEngine.VKE_NAMESPACE, IMAGE_TO_SCREEN_KEY);

    public static final Logger LOGGER = LoggerFactory.get("Rendering/RenderGraph");

    protected final RenderSystem sys;
    protected final RenderGraph graph;

    private final Def def;

    private Texture[] inputs;
    private OutputTexture[] outputs;

    public RenderPass(RenderSystem sys, RenderGraph graph, Def def) {
        this.sys = sys;
        this.graph = graph;
        this.def = def;
    }

    public GraphTexture searchInputTexture(String name) {
        for (int i = 0; i < def.inputs.length; i++) {
            if (def.inputs[i].name.equals(name)) {
                return new GraphTexture(sys, TextureCategory.Input, this, i);
            }
        }
        throw new IllegalArgumentException("Input texture '" + name + "' not found in RenderPass '" + def.name + "'!");
    }

    public GraphTexture searchOutputTexture(String name) {
        for (int i = 0; i < def.outputs.length; i++) {
            if (def.outputs[i].name.equals(name)) {
                return new GraphTexture(sys, TextureCategory.Output, this, i);
            }
        }
        throw new IllegalArgumentException("Output texture '" + name + "' not found in RenderPass '" + def.name + "'!");
    }

    public Def getDefinition() {
        return def;
    }

    public RenderSystem getRenderSystem() {
        return sys;
    }

    private Texture findSourceTexture(SegmentedPath path, RenderGraph graph) {
        String[] parts = path.getParts();
        String renderPassName = parts[0];
        String textureName = parts[1];

        RenderPass pass = graph.searchRenderPass(sys.id(renderPassName));
        GraphTexture tex = pass.searchOutputTexture(textureName);
        return tex.extract();
    }

    public void buildInputTextures(RenderGraph graph) {
        this.inputs = new Texture[def.inputs.length];

        for (int i = 0; i < def.inputs.length; i++) {
            InputTextureDef texDef = def.inputs[i];
            SegmentedPath source = texDef.source;
            Texture tex = findSourceTexture(source, graph);

            if (tex == null) {
                throw new IllegalStateException("Render pass '" + def.name() + "' requires input '" + source + "' but it was never created!");
            }

            inputs[i] = tex;
        }
    }

    public void buildOutputTextures(int windowWidth, int windowHeight, RenderGraph graph) {
        this.outputs = new OutputTexture[def.outputs.length];

        for (int i = 0; i < def.outputs.length; i++) {
            OutputTextureDef texDef = def.outputs[i];
            Texture tex;

            SegmentedPath source = texDef.source();

            TextureType type = texDef.type;
            if (type == TextureType.SCREEN) {
                if (source != null) {
                    LOGGER.fatal("SCREEN texture cannot have a source! Im talking about texture '%s' of '%s' in '%s'.", texDef.name, def.name, graph.getName());
                    sys.getEngine().explode();
                }

                outputs[i] = new OutputTexture(graph.getAllScreenTextures(), false);
            } else {
                if (source != null) {
                    tex = findSourceTexture(source, graph);
                } else {
                    float scale = texDef.scale();
                    int width = texDef.width() == 0 ? (int) (scale * windowWidth) : texDef.width();
                    int height = texDef.height() == 0 ? (int) (scale * windowHeight) : texDef.height();

                    tex = graph.getTextureAllocation(width, height, texDef.type(), texDef.format());
                }
                if (tex == null) {
                    LOGGER.error("Texture '%s' of '%s' in '%s' couldnt be found!", source, def.name, graph.getName());
                }

                outputs[i] = new OutputTexture(new Texture[]{ tex }, source != null);
            }
        }
    }

    public void releaseTextures(RenderGraph graph) {
        if (inputs == null) return;

        for (Texture input : inputs) {
            graph.releaseTexture(input);
        }

        if (outputs == null) return;

        for (int i = 0; i < outputs.length; i++) {
            OutputTexture output = outputs[i];
            OutputTextureDef texDef = def.outputs[i];
            if (texDef.type != TextureType.SCREEN) {
                if (output != null && output.texture != null) {
                    for (Texture outputTexture : output.texture) {
                        graph.releaseTexture(outputTexture);
                    }
                }
            }
        }
    }

    public Texture getInputTexture(int texture) {
        return inputs[texture];
    }

    public Texture getOutputTexture(int texture, int imageIndex) {
        var texs = outputs[texture].texture;
        return texs[Math.min(imageIndex, texs.length - 1)];
    }

    public boolean hasOutputSource(int texture) {
        return outputs[texture].hasSource;
    }

    public LoadOp getLoadOp(int texture) {
        return hasOutputSource(texture) ? LoadOp.LOAD : LoadOp.CLEAR;
    }

    public abstract void onLoad();

    public void beforeExecute() {
        for (Texture input : inputs) {
            if (input != null) {
                input.useInShader();
            }
        }
    }

    public abstract void execute(CommandBuffer cmd, GraphContext context);

    public record OutputTexture(Texture[] texture, boolean hasSource) {}

    public static final class Def {
        private final int order;
        private final Identifier name;
        private final Identifier parent;
        private final @Nullable String clazz;
        private InputTextureDef[] inputs;
        private int inputsLength;
        private OutputTextureDef[] outputs;
        private int outputsLength;
        private @Nullable ConfigArrayNode data;

        public Def(int order, Identifier name, Identifier parent, @Nullable String clazz, InputTextureDef[] inputs, OutputTextureDef[] outputs, @Nullable ConfigArrayNode data) {
            this.order = order;
            this.name = name;
            this.parent = parent;
            this.clazz = clazz;
            this.inputs = inputs;
            this.inputsLength = inputs.length;
            this.outputs = outputs;
            this.outputsLength = outputs.length;
            this.data = data;
        }

        public Def extend(Def parent) {
                ArrayList<InputTextureDef> in = new ArrayList<>(List.of(inputs));
                HashSet<String> inputNames = new HashSet<>(Arrays.stream(inputs).map(InputTextureDef::name).toList());

                for (InputTextureDef input : parent.inputs) {
                    if (!inputNames.add(input.name)) {
                        LOGGER.warn("RenderPass '%s' overwrites input texture '%s' from its parent '%s'! Consider removing the duplicate", name, input.name, parent.name);
                    }
                    in.add(input);
                }

                ArrayList<OutputTextureDef> out = new ArrayList<>(List.of(outputs));
                HashSet<String> outputNames = new HashSet<>(Arrays.stream(outputs).map(OutputTextureDef::name).toList());

                for (OutputTextureDef output : parent.outputs) {
                    if (!outputNames.add(output.name)) {
                        LOGGER.warn("RenderPass '%s' overwrites output texture '%s' from its parent '%s'! Consider removing the duplicate", name, output.name, parent.name);
                    }
                    out.add(output);
                }

                InputTextureDef[] inArr = in.toArray(InputTextureDef[]::new);
                OutputTextureDef[] outArr = out.toArray(OutputTextureDef[]::new);

                ConfigArrayNode data = Configs.mergeArrays(this.data, parent.data);

                String clazz = this.clazz;
                if (clazz == null) {
                    clazz = parent.clazz;
                }

                return new Def(order, name, parent.parent, clazz, inArr, outArr, data);
            }

        public Identifier name() {
            return name;
        }

        public Identifier parent() {
            return parent;
        }

        public @Nullable String clazz() {
            return clazz;
        }

        public InputTextureDef[] inputs() {
            return inputs;
        }

        public OutputTextureDef[] outputs() {
            return outputs;
        }

        public @Nullable ConfigArrayNode data() {
            return data;
        }

        public void allocateOutputTextures(int n) {
            this.outputs = Arrays.copyOf(this.outputs, this.outputs.length + n);
        }

        public void allocateInputTextures(int n) {
            this.inputs = Arrays.copyOf(this.inputs, this.inputs.length + n);
        }

        public void addOutputTexture(OutputTextureDef def) {
            int missing = outputsLength - outputs.length + 1;
            if (missing > 0) {
                allocateOutputTextures(missing);
            }
            outputs[outputsLength] = def;
            outputsLength++;
        }

        public void addInputTexture(InputTextureDef def) {
            int missing = inputsLength - inputs.length + 1;
            if (missing > 0) {
                allocateInputTextures(missing);
            }
            inputs[inputsLength] = def;
            inputsLength++;
        }

        public void mergeData(ConfigArrayNode extraData) {
            this.data = Configs.mergeArrays(this.data, extraData);
        }

        public int order() {
            return order;
        }
    }

    public void beginRendering(CommandBuffer cmd, List<GraphTexture> color, RgbColor clear) {
        this.beginRendering(cmd, color, null, null, List.of(clear), null, null);
    }

    public void beginRendering(CommandBuffer cmd, List<GraphTexture> color, GraphTexture depth, RgbColor clear, RgbColor depthClear) {
        this.beginRendering(cmd, color, depth, null, List.of(clear), depthClear, null);
    }

    public void beginRendering(CommandBuffer cmd, List<GraphTexture> color, GraphTexture depth, List<RgbColor> clear, RgbColor depthClear) {
        this.beginRendering(cmd, color, depth, null, clear, depthClear, null);
    }

    public void beginRendering(CommandBuffer cmd, List<GraphTexture> color, GraphTexture depth, GraphTexture stencil, List<RgbColor> clear, RgbColor depthClear, RgbColor stencilClear) {
        List<CommandBuffer.AttachmentInfo> colorInfos = new ArrayList<>();
        CommandBuffer.AttachmentInfo da = null, sa = null;
        for (int i = 0; i < color.size(); i++) {
            GraphTexture s = color.get(i);
            colorInfos.add(new CommandBuffer.AttachmentInfo(s.extract(), getLoadOp(s.getIndex()), StoreOp.STORE, clear.get(i).getComponents()));
        }

        if (depth != null) {
            da = new CommandBuffer.AttachmentInfo(depth.extract(), getLoadOp(depth.getIndex()), StoreOp.STORE, depthClear.getComponents());
        }

        if (stencil != null) {
            sa = new CommandBuffer.AttachmentInfo(stencil.extract(), getLoadOp(stencil.getIndex()), StoreOp.STORE, stencilClear.getComponents());
        }

        cmd.beginRendering(new CommandBuffer.RenderingInfo(colorInfos, da, sa));
    }

    public record InputTextureDef(String name, SegmentedPath source) {}
    public record OutputTextureDef(String name, @Nullable SegmentedPath source, @Nullable TextureType type, @Nullable Format format, int width, int height, float scale) {}

    public enum TextureType {
        SCREEN("screen"),
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
            if (name == null) return COLOR;
            try {
                return TextureType.valueOf(name);
            } catch (IllegalArgumentException _) {}
            for (TextureType value : values()) {
                if (value.name.equalsIgnoreCase(name)) return value;
            }
            return COLOR;
        }
    }
}
