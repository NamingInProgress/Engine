package com.vke.core.rendering.graph2.parse;

import com.vke.api.assets.r.R;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.assets.handles.LazyAssetHandle;
import com.vke.core.parsing.config.utils.EmptyConfigArray;
import com.vke.core.parsing.config.utils.GeneralConfigArrayNode;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.graph2.renderpass.ImageToScreenRenderPass;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.utils.io.SegmentedPath;

public class Graph2Parser {
    private static final LazyAssetHandle<ConfigSchema> RENDER_PASS_SCHEMA = R.schemas.get("render-pass.vks");
    private static final LazyAssetHandle<ConfigSchema> RENDER_GRAPH_SCHEMA = R.schemas.get("render-graph.vks");

    public static RenderPass.Def parseRenderPass(Context context, ConfigDocument document) throws SchemaMismatchException {
        ConfigNode node = document.getRoot().getObject("render-pass");
        document.validate(RENDER_PASS_SCHEMA.assume(context), document.getIdentifier());

        return parseRenderPass0(context, node, 0);
    }

    private static RenderPass.Def parseRenderPass0(Context context, ConfigNode node, int order) {
        String name = node.getStringOption("name").expect("schema will carry");
        String parent = node.getStringOption("parent").unwrapOrNull();
        if (name.equals(RenderPass.IMAGE_TO_SCREEN_KEY)) {
            throw new IllegalArgumentException("RenderPasses cant have the name '" + name + "'! This is a reserved name. Please use the <image-to-screen source=\"...\"/> shortcut instead.");
        }

        Identifier nameIdent = context.id(name);
        Identifier parentIdent = null;
        if (parent != null) {
            parentIdent = context.id(parent);
        }

        ConfigArrayNode inputsArr = node.getArrayOption("inputs").unwrapOrElse(EmptyConfigArray::new);
        ConfigArrayNode outputsArr = node.getArrayOption("outputs").unwrapOrElse(EmptyConfigArray::new);

        ConfigNode[] inputs = inputsArr.values();
        ConfigNode[] outputs = outputsArr.values();

        String className = node.getStringOption("class").unwrapOrNull();

        ConfigArrayNode data = node.getArrayOption("data").unwrapOrNull();

        RenderPass.InputTextureDef[] inputTex = new RenderPass.InputTextureDef[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            inputTex[i] = buildInputTextures(inputs[i]);
        }

        RenderPass.OutputTextureDef[] outputTex = new RenderPass.OutputTextureDef[outputs.length];
        for (int i = 0; i < outputs.length; i++) {
            outputTex[i] = buildOutputTextures(outputs[i]);
        }

        return new RenderPass.Def(order, nameIdent, parentIdent, className, inputTex, outputTex, data);
    }

    private static RenderPass.InputTextureDef buildInputTextures(ConfigNode input) {
        String name = input.getString("name");
        String source = input.getStringOption("source").unwrapOrNull();
        SegmentedPath sourcePath = new SegmentedPath(source);
        return new RenderPass.InputTextureDef(name, sourcePath);
    }

    private static RenderPass.OutputTextureDef buildOutputTextures(ConfigNode output) {
        String name = output.getString("name");
        if (output.getNodeName().equalsIgnoreCase("screen")) {
            return new RenderPass.OutputTextureDef(name, null,
                    RenderPass.TextureType.SCREEN, Format.BGRA8_SRGB, 0, 0, 1);
        }
        SegmentedPath sourcePath = output.getStringOption("source").map(SegmentedPath::new).unwrapOrNull();

        int width = output.getIntOption("width").unwrapOrDefault();
        int height = output.getIntOption("height").unwrapOrDefault();
        float scale = output.getNumberOption("scale").unwrapOr(1f);
        RenderPass.TextureType type = RenderPass.TextureType.fromString(output.getString("type"));
        Format format = Format.valueOfOption(output.getStringOption("format").unwrapOrNull()).unwrapOr(Format.RGBA16F);

        return new RenderPass.OutputTextureDef(name, sourcePath, type, format, width, height, scale);
    }

    public static RenderGraph.Def parseRenderGraph(Context context, ConfigDocument document, RenderPassReconstructor globalReconstructor) throws SchemaMismatchException {
        ConfigNode node = document.getRoot().getObject("render-graph");
        document.validate(RENDER_GRAPH_SCHEMA.assume(context), document.getIdentifier());

        return parseRenderGraph0(context, node, globalReconstructor);
    }

    private static RenderGraph.Def parseRenderGraph0(Context context, ConfigNode node, RenderPassReconstructor globalReconstructor) {
        String name = node.getStringOption("name").expect("schema");
        ConfigArrayNode passes = node.getArray("passes");

        ConfigNode[] values = passes.values();
        RenderPass.Def[] defs = new RenderPass.Def[values.length];
        RenderPassReconstructor reconstructor = new RenderPassReconstructor(globalReconstructor);
        for (int i = 0; i < values.length; i++) {
            ConfigNode passNode = values[i];
            String nodeName = passNode.getNodeName();
            if (RenderPass.IMAGE_TO_SCREEN_KEY.equals(nodeName)) {
                defs[i] = new RenderPass.Def(
                        i,
                        RenderPass.IMAGE_TO_SCREEN_IDENT,
                        null,
                        ImageToScreenRenderPass.class.getName(),
                        new RenderPass.InputTextureDef[0],
                        new RenderPass.OutputTextureDef[0],
                        //its fine to put passNode here as data because it has the "source" attrib on it
                        new GeneralConfigArrayNode(new ConfigNode[]{ passNode })
                );
            } else {
                defs[i] = parseRenderPass0(context, passNode, i);
            }

            reconstructor.onRenderPassParsed(defs[i]);
        }

        Identifier nameIdent = context.id(name);
        return new RenderGraph.Def(nameIdent, reconstructor);
    }
}
