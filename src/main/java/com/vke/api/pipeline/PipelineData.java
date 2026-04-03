package com.vke.api.pipeline;

import com.vke.api.rendering.vulkan.descriptors.info.DescriptorSetLayout;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorsInfo;
import com.vke.api.rendering.abstraction.enums.CompareOp;
import com.vke.api.rendering.abstraction.enums.ShaderType;
import com.vke.api.rendering.abstraction.enums.texture.TextureFormat;
import com.vke.api.assets.AssetHandle;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.*;
import com.vke.api.rendering.vulkan.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.shaders.ShaderProgram;
import com.vke.core.assets.handles.rendering.shader.ShaderProgramAssetHandle;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.helpers.Option;
import org.lwjgl.vulkan.VK14;

import java.util.*;

public class PipelineData {

    private ArrayList<DescriptorSetLayout> descriptorLayouts;
    private DescriptorsInfo additionalDescriptorInfo;
    private PushConstantsData pushConstantsData;
    private VertexLayoutData vertexLayoutData;

    // pipeline fields here

    // Dynamic State
    public ArrayList<RenderPipeline.DynamicState> dynamicStates = new ArrayList<>(List.of(RenderPipeline.DynamicState.VIEWPORT, RenderPipeline.DynamicState.SCISSOR));

    // Input Assembly
    public boolean primitiveRestartEnable = false;
    public RenderPipeline.Topology topology;

    // Raster Info
    public RenderPipeline.PolygonMode polygonMode = RenderPipeline.PolygonMode.FILL;
    public RenderPipeline.CullMode cullMode = RenderPipeline.CullMode.BACK;
    public RenderPipeline.WindingOrder windingOrder = RenderPipeline.WindingOrder.COUNTERCLOCKWISE;
    public float lineWidth = 1.0f;
    public boolean depthBiasEnable = false;
    public float depthBiasConstFactor = 0.0f;
    public float depthBiasClamp = 0.0f;
    public float depthBiasSlopeFactor = 0.0f;

    // Attachments
    public boolean stencilAttachment = false;
    public ArrayList<AttachmentInfo> attachments;
    public float[] blendConstants = new float[]{ 0, 0, 0, 0 };
    public boolean autoRegisterDynamicStates = false;

    // Shader
    public AssetHandle<ShaderProgram> shaders;

    private static final String
            DYNAMIC_STATES_ARRAY_NAME = "dynamicStates",
            PRIMITVE_RESTART_ENABLE_NAME = "primitiveRestartEnable",
            TOPOLOGY_NAME = "topology",
            POLYGON_MODE_NAME = "polygonMode",
            CULL_MODE_NAME = "cullMode",
            WINDING_ORDER_NAME = "windingOrder",
            LINE_WIDTH_NAME = "lineWidth",
            DEPTH_BIAS_ENABLE_NAME = "depthBiasEnable",
            DEPTH_BIAS_CONST_FAC_NAME = "depthBiasConstFactor",
            DEPTH_BIAS_CLAMP_NAME = "depthBiasClamp",
            DEPTH_BIAS_SLOPE_FAC_NAME = "depthBiasSlopeFactor",
            ATTACHMENTS_ARRAY_NAME = "attachments",
            HAS_STENCIL_ATTACHMENT_NAME = "stencilAttachment",
            AUTO_REGISTER_DYNAMIC_STATES_NAME = "autoRegisterDynamicStates",
            SHADERS_ARRAY_NAME = "shaders",
            BLEND_CONSTANTS_NAME = "blendConstants",
            DYNAMIC_BUFFERS_ARRAY_NAME = "dynamicBuffers",
            RUNTIME_SIZE_ARRAYS_NAME = "runtimeSizeArrays";

    private static final String
            SHADERS_ARRAY_IDENTIFIER_NAME = "src",
            SHADERS_ARRAY_TYPE_NAME = "type";

    private static final String
            RUNTIME_SIZE_ARRAYS_NAME_NAME = "name",
            RUNTIME_SIZE_ARRAYS_SIZE_NAME = "size";

    public static PipelineData fromConfig(ConfigDocument document) {
        ConfigNode root = document.getRoot();
        PipelineData pd = new PipelineData();
        pd.dynamicStates = dynamicStates(root, pd.dynamicStates);
        pd.primitiveRestartEnable = root.getBooleanOption(PRIMITVE_RESTART_ENABLE_NAME).unwrapOr(pd.primitiveRestartEnable);
        pd.topology = RenderPipeline.Topology.valueOf(root.getString(TOPOLOGY_NAME));
        pd.polygonMode = RenderPipeline.PolygonMode.valueOfOption(root.getString(POLYGON_MODE_NAME)).unwrapOr(pd.polygonMode);
        pd.cullMode = RenderPipeline.CullMode.valueOfOption(root.getString(CULL_MODE_NAME)).unwrapOr(pd.cullMode);
        pd.windingOrder = RenderPipeline.WindingOrder.valueOfOption(root.getString(WINDING_ORDER_NAME)).unwrapOr(pd.windingOrder);
        pd.lineWidth = root.getNumberOption(LINE_WIDTH_NAME).unwrapOr(pd.lineWidth);
        pd.depthBiasEnable = root.getBooleanOption(DEPTH_BIAS_ENABLE_NAME).unwrapOr(pd.depthBiasEnable);
        pd.depthBiasConstFactor = root.getNumberOption(DEPTH_BIAS_CONST_FAC_NAME).unwrapOr(pd.depthBiasConstFactor);
        pd.depthBiasClamp = root.getNumberOption(DEPTH_BIAS_CLAMP_NAME).unwrapOr(pd.depthBiasClamp);
        pd.depthBiasSlopeFactor = root.getNumberOption(DEPTH_BIAS_SLOPE_FAC_NAME).unwrapOr(pd.depthBiasSlopeFactor);
        pd.stencilAttachment = root.getBooleanOption(HAS_STENCIL_ATTACHMENT_NAME).unwrapOr(pd.stencilAttachment);
        pd.autoRegisterDynamicStates = root.getBooleanOption(AUTO_REGISTER_DYNAMIC_STATES_NAME).unwrapOr(pd.autoRegisterDynamicStates);
        pd.blendConstants = float4OrDefault(root, BLEND_CONSTANTS_NAME, pd.blendConstants);
        pd.shaders = shaders(root);
        pd.attachments = attachments(root);
        pd.additionalDescriptorInfo = descriptorsInfo(root);
        return pd;
    }

    private static ArrayList<RenderPipeline.DynamicState> dynamicStates(ConfigNode parent, ArrayList<RenderPipeline.DynamicState> defaultValue) {
        Option<ConfigArrayNode> arrNodeOpt = parent.getArrayOption(DYNAMIC_STATES_ARRAY_NAME);
        if (arrNodeOpt.isNone()) return defaultValue;
        ConfigArrayNode arrNode = arrNodeOpt.unwrap();
        ArrayList<RenderPipeline.DynamicState> states = new ArrayList<>();
        for (ConfigNode node : arrNode.values()) {
            RenderPipeline.DynamicState s = RenderPipeline.DynamicState.valueOf(node.asString());
            states.add(s);
        }
        return states;
    }

    private static float[] float4OrDefault(ConfigNode parent, String fieldName, float[] defaultValue) {
        Option<ConfigArrayNode> arrNodeOpt = parent.getArrayOption(fieldName);
        if (arrNodeOpt.isNone()) return defaultValue;
        ConfigArrayNode arrNode = arrNodeOpt.unwrap();
        ConfigNode[] nodes = arrNode.values();
        float[] thingies = new float[4];
        int i = 0;
        for(ConfigNode n : nodes) {
            if (n instanceof ConfigNumberNode cnn) {
                thingies[i++] = cnn.getValue();
            }
            if(i >= 4) {
                break;
            }
        }
        return thingies;
    }

    private static AssetHandle<ShaderProgram> shaders(ConfigNode parent) {
        Option<ConfigArrayNode> arrNodeOpt = parent.getArrayOption(SHADERS_ARRAY_NAME);
        if (arrNodeOpt.isNone()) return null;
        ConfigArrayNode arrNode = arrNodeOpt.unwrap();
        Map<ShaderType, Identifier> shaderSources = new HashMap<>();
        for(ConfigNode node : arrNode.values()) {
            ShaderType type = ShaderType.fromString(node.getString(SHADERS_ARRAY_TYPE_NAME));
            Identifier identifier = Identifier.of(node.getString(SHADERS_ARRAY_IDENTIFIER_NAME));
            shaderSources.put(type, identifier);
        }
        return new ShaderProgramAssetHandle(shaderSources);
    }

    public static DescriptorsInfo descriptorsInfo(ConfigNode parent) {
        DescriptorsInfo di = new DescriptorsInfo();

        Option<ConfigArrayNode> dynamicBufferOptional = parent.getArrayOption(DYNAMIC_BUFFERS_ARRAY_NAME);
        if (dynamicBufferOptional.isSome()) {
            for (ConfigNode n : dynamicBufferOptional.unwrap().values()) {
                String name = n.asString();
                di.dynamicBuffers.add(name);
            }
        }

        Option<ConfigArrayNode> runtimeSizeArraysOptional = parent.getArrayOption(RUNTIME_SIZE_ARRAYS_NAME);
        if (runtimeSizeArraysOptional.isSome()) {
            for (ConfigNode n : runtimeSizeArraysOptional.unwrap().values()) {
                ConfigObjectNode obj = n.asObject();
                String name = obj.getString(RUNTIME_SIZE_ARRAYS_NAME_NAME);
                int size = obj.getInt(RUNTIME_SIZE_ARRAYS_SIZE_NAME);

                if (size < 1) throw new IllegalStateException("Cannot create runtime size array of size " + size);

                di.runtimeSizeArraySizes.put(name, size);
            }
        }

        return di;
    }

    public static ArrayList<AttachmentInfo> attachments(ConfigNode parent) {
        ArrayList<AttachmentInfo> attachments = new ArrayList<>();
        Option<ConfigArrayNode> arrNodeOpt = parent.getArrayOption(ATTACHMENTS_ARRAY_NAME);
        if (arrNodeOpt.isNone()) return attachments;
        ConfigArrayNode arrNode = arrNodeOpt.unwrap();

        for (ConfigNode value : arrNode.values()) {
            ConfigObjectNode attachmentData  = value.asObject();

            Option<String> typeOpt = attachmentData.getStringOption("type");
            if (typeOpt.isNone()) throw new IllegalStateException("Missing type in attachment definition!");

            switch (typeOpt.unwrap()) {
                case "COLOR" -> attachments.add(new ColorAttachmentInfo(attachmentData));
                case "DEPTH" -> attachments.add(new DepthAttachmentInfo(attachmentData));
                case "STENCIL" -> attachments.add(new StencilAttachmentInfo(attachmentData));

            }
        }

        return attachments;
    }

    public static abstract class AttachmentInfo {
        public AttachmentType type;

        public AttachmentInfo(ConfigObjectNode c) {}
    }

    public static class ColorAttachmentInfo extends AttachmentInfo {
        public static final String
                COLOR_WRITE_MASK_ARRAY_NAME = "colorWriteMask",
                BLEND_ENABLE_NAME = "blendEnable",
                SRC_BLEND_FACTOR_NAME = "srcBlendFactor",
                DST_BLEND_FACTOR_NAME = "dstBlendFactor",
                SRC_ALPHA_BLEND_FACTOR_NAME = "srcAlphaBlendFactor",
                DST_ALPHA_BLEND_FACTOR_NAME = "dstAlphaBlendFactor",
                COLOR_BLEND_OPERATION_NAME = "colorBlendOperation",
                ALPHA_BLEND_OPERATION_NAME = "alphaBlendOperation",
                TEXTURE_FORMAT_NAME = "format";

        public int colorWriteMask = VK14.VK_COLOR_COMPONENT_R_BIT | VK14.VK_COLOR_COMPONENT_G_BIT | VK14.VK_COLOR_COMPONENT_B_BIT | VK14.VK_COLOR_COMPONENT_A_BIT;
        public boolean blendEnable = true;
        public RenderPipeline.BlendFactor srcBlendFactor = RenderPipeline.BlendFactor.SRC_ALPHA;
        public RenderPipeline.BlendFactor dstBlendFactor = RenderPipeline.BlendFactor.ONE_MINUS_SRC_ALPHA;
        public RenderPipeline.BlendFactor srcAlphaBlendFactor = RenderPipeline.BlendFactor.ONE;
        public RenderPipeline.BlendFactor dstAlphaBlendFactor = RenderPipeline.BlendFactor.ONE_MINUS_SRC_ALPHA;
        public RenderPipeline.BlendOperation colorBlendOperation = RenderPipeline.BlendOperation.ADD;
        public RenderPipeline.BlendOperation alphaBlendOperation = RenderPipeline.BlendOperation.ADD;
        public TextureFormat format = TextureFormat.BGRA8_SRGB;

        public ColorAttachmentInfo(ConfigObjectNode c) {
            super(c);

            Option<ConfigArrayNode> colorWriteMaskArrayOpt = c.getArrayOption(COLOR_WRITE_MASK_ARRAY_NAME);
            if (colorWriteMaskArrayOpt.isSome())
                this.colorWriteMask = Arrays.stream(colorWriteMaskArrayOpt.unwrap().values())
                        .mapToInt((sth) -> (int) sth.asNumber()).reduce(0, (a, b) -> a | b);

            this.blendEnable = c.getBooleanOption(BLEND_ENABLE_NAME).unwrapOr(blendEnable);

            this.srcBlendFactor = RenderPipeline.BlendFactor.valueOfOption(c.getString(SRC_BLEND_FACTOR_NAME)).unwrapOr(srcBlendFactor);
            this.dstBlendFactor = RenderPipeline.BlendFactor.valueOfOption(c.getString(DST_BLEND_FACTOR_NAME)).unwrapOr(dstBlendFactor);
            this.srcAlphaBlendFactor = RenderPipeline.BlendFactor.valueOfOption(c.getString(SRC_ALPHA_BLEND_FACTOR_NAME)).unwrapOr(srcAlphaBlendFactor);
            this.dstAlphaBlendFactor = RenderPipeline.BlendFactor.valueOfOption(c.getString(DST_ALPHA_BLEND_FACTOR_NAME)).unwrapOr(dstAlphaBlendFactor);

            this.colorBlendOperation = RenderPipeline.BlendOperation.valueOfOption(c.getString(COLOR_BLEND_OPERATION_NAME)).unwrapOr(colorBlendOperation);
            this.alphaBlendOperation = RenderPipeline.BlendOperation.valueOfOption(c.getString(ALPHA_BLEND_OPERATION_NAME)).unwrapOr(alphaBlendOperation);

            this.format = TextureFormat.valueOfOption(c.getString(TEXTURE_FORMAT_NAME)).unwrapOr(format);
        }
    }

    // So for pipeline you can just combine them and then when starting the command buffers that's when you separate the images
    public static class DepthAttachmentInfo extends AttachmentInfo {
        public static final String
                DEPTH_TEST_ENABLE_NAME = "depthTestEnable",
                DEPTH_WRITE_ENABLE_NAME = "depthWriteEnable",
                DEPTH_COMPARE_OP_NAME = "depthCompareOperation";

        public boolean depthTestEnable = true;
        public boolean depthWriteEnable = true;
        public CompareOp depthCompareOp = CompareOp.LEQUAL;

        public DepthAttachmentInfo(ConfigObjectNode c) {
            super(c);

            this.depthTestEnable = c.getBooleanOption(DEPTH_TEST_ENABLE_NAME).unwrapOr(depthTestEnable);
            this.depthWriteEnable = c.getBooleanOption(DEPTH_WRITE_ENABLE_NAME).unwrapOr(depthWriteEnable);
            this.depthCompareOp = CompareOp.valueOfOption(c.getString(DEPTH_COMPARE_OP_NAME)).unwrapOr(depthCompareOp);
        }
    }

    public static class StencilAttachmentInfo extends AttachmentInfo {
        public static final String
                STENCIL_TEST_ENABLE_NAME = "stencilTestEnable",
                FRONT_STENCIL_OP_NAME = "frontOperation",
                BACK_STENCIL_OP_NAME = "backOperation";

        public static final String
                FAIL_OP_NAME = "failOp",
                PASS_OP_NAME = "passOp",
                DEPTH_FAIL_OP_NAME = "depthFailOp",
                COMPARE_OP_NAME = "compareOp",
                COMPARE_MASK_NAME = "compareMask",
                WRITE_MASK_NAME = "writeMask",
                REFERENCE_NAME = "reference";

        boolean stencilTestEnable = false;
        RenderPipeline.StencilOpState frontStencilOp = new RenderPipeline.StencilOpState();
        RenderPipeline.StencilOpState backStencilOp = new RenderPipeline.StencilOpState();

        public StencilAttachmentInfo(ConfigObjectNode c) {
            super(c);

            this.stencilTestEnable = c.getBooleanOption(STENCIL_TEST_ENABLE_NAME).unwrapOr(stencilTestEnable);
            this.frontStencilOp = getOpState(frontStencilOp, c.getObject(FRONT_STENCIL_OP_NAME));
            this.backStencilOp = getOpState(backStencilOp, c.getObject(BACK_STENCIL_OP_NAME));
        }

        private static RenderPipeline.StencilOpState getOpState(RenderPipeline.StencilOpState defaultState, ConfigObjectNode c) {
            if (c == null) return defaultState;

            RenderPipeline.StencilOp failOp = RenderPipeline.StencilOp.valueOfOption(c.getString(FAIL_OP_NAME))
                    .unwrapOr(defaultState.failOp());
            RenderPipeline.StencilOp passOp = RenderPipeline.StencilOp.valueOfOption(c.getString(PASS_OP_NAME))
                    .unwrapOr(defaultState.passOp());
            RenderPipeline.StencilOp depthFailOp = RenderPipeline.StencilOp.valueOfOption(c.getString(DEPTH_FAIL_OP_NAME))
                    .unwrapOr(defaultState.depthFailOp());
            CompareOp compareOp = CompareOp.valueOfOption(c.getString(COMPARE_OP_NAME)).unwrapOr(defaultState.compareOp());
            int compareMask = c.getIntOption(COMPARE_MASK_NAME).unwrapOr(defaultState.compareMask());
            int writeMask = c.getIntOption(WRITE_MASK_NAME).unwrapOr(defaultState.writeMask());
            int reference = c.getIntOption(REFERENCE_NAME).unwrapOr(defaultState.reference());

            return new RenderPipeline.StencilOpState(failOp, passOp, depthFailOp, compareOp,
                                            compareMask, writeMask, reference);
        }

    }

    public enum AttachmentType {
        COLOR,
        DEPTH,
        STENCIL,
        DEPTH_STENCIL
    }

}
