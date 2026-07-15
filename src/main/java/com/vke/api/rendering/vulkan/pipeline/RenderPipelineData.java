package com.vke.api.rendering.vulkan.pipeline;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.api.rendering.abstraction.renderer.shader.Shader;
import com.vke.api.rendering.abstraction.renderer.shader.ShaderProgram;
import com.vke.api.rendering.vulkan.descriptors.info.DescriptorsInfo;
import com.vke.api.rendering.abstraction.renderer.enums.CompareOp;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.api.assets.AssetHandle;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.*;
import com.vke.core.rendering.vulkan.shader.VKShaderProgram;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;
import com.vke.utils.tuple.Pair;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkStencilOpState;

import java.util.*;

public class RenderPipelineData {

    // WARNING! THESE FIELDS ARE RESOLVED ONLY DURING PIPELINE CREATION, WHICH MEANS THEY WILL NOT BE AVAILABLE BEFOREHAND!
    public DescriptorsInfo additionalDescriptorInfo;
    public VertexLayoutData vertexLayoutData;
    public VKShaderProgram compiledShaders;

    // pipeline fields here

    // Dynamic State
    public ArrayList<DynamicState> dynamicStates = new ArrayList<>(List.of(DynamicState.VIEWPORT, DynamicState.SCISSOR));

    // Input Assembly
    public boolean primitiveRestartEnable = false;
    public Topology topology;

    // Raster Info
    public PolygonMode polygonMode = PolygonMode.FILL;
    public CullMode cullMode = CullMode.BACK;
    public WindingOrder windingOrder = WindingOrder.COUNTERCLOCKWISE;
    public float lineWidth = 1.0f;
    public boolean depthBiasEnable = false;
    public float depthBiasConstFactor = 0.0f;
    public float depthBiasClamp = 0.0f;
    public float depthBiasSlopeFactor = 0.0f;

    // Attachments
    public ArrayList<ColorAttachmentInfo> colorAttachments;
    public DepthAttachmentInfo depthAttachment;
    public StencilAttachmentInfo stencilAttachment;
    public float[] blendConstants = new float[]{ 0, 0, 0, 0 };
    public boolean autoRegisterDynamicStates = false;

    // Shader
    public ShaderProgram shaders;

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

    public static RenderPipelineData fromConfig(ConfigDocument document) {
        ConfigNode root = document.getRoot();
        RenderPipelineData pd = new RenderPipelineData();
        pd.dynamicStates = dynamicStates(root, pd.dynamicStates);
        pd.primitiveRestartEnable = root.getBooleanOption(PRIMITVE_RESTART_ENABLE_NAME).unwrapOr(pd.primitiveRestartEnable);
        pd.topology = Topology.valueOf(root.getString(TOPOLOGY_NAME));
        pd.polygonMode = PolygonMode.valueOfOption(root.getString(POLYGON_MODE_NAME)).unwrapOr(pd.polygonMode);
        pd.cullMode = CullMode.valueOfOption(root.getString(CULL_MODE_NAME)).unwrapOr(pd.cullMode);
        pd.windingOrder = WindingOrder.valueOfOption(root.getString(WINDING_ORDER_NAME)).unwrapOr(pd.windingOrder);
        pd.lineWidth = root.getNumberOption(LINE_WIDTH_NAME).unwrapOr(pd.lineWidth);
        pd.depthBiasEnable = root.getBooleanOption(DEPTH_BIAS_ENABLE_NAME).unwrapOr(pd.depthBiasEnable);
        pd.depthBiasConstFactor = root.getNumberOption(DEPTH_BIAS_CONST_FAC_NAME).unwrapOr(pd.depthBiasConstFactor);
        pd.depthBiasClamp = root.getNumberOption(DEPTH_BIAS_CLAMP_NAME).unwrapOr(pd.depthBiasClamp);
        pd.depthBiasSlopeFactor = root.getNumberOption(DEPTH_BIAS_SLOPE_FAC_NAME).unwrapOr(pd.depthBiasSlopeFactor);
        pd.autoRegisterDynamicStates = root.getBooleanOption(AUTO_REGISTER_DYNAMIC_STATES_NAME).unwrapOr(pd.autoRegisterDynamicStates);
        pd.blendConstants = float4OrDefault(root, BLEND_CONSTANTS_NAME, pd.blendConstants);
        pd.shaders = shaders(root);
        pd.colorAttachments = colorAttachments(root);

        var depthStencil = getDepthStencilAttachments(root);
        pd.depthAttachment = depthStencil.v1;
        pd.stencilAttachment = depthStencil.v2;

        pd.additionalDescriptorInfo = descriptorsInfo(root);
        return pd;
    }

    private static ArrayList<DynamicState> dynamicStates(ConfigNode parent, ArrayList<DynamicState> defaultValue) {
        Option<ConfigArrayNode> arrNodeOpt = parent.getArrayOption(DYNAMIC_STATES_ARRAY_NAME);
        if (arrNodeOpt.isNone()) return defaultValue;
        ConfigArrayNode arrNode = arrNodeOpt.unwrap();
        ArrayList<DynamicState> states = new ArrayList<>();
        for (ConfigNode node : arrNode.values()) {
            DynamicState s = DynamicState.valueOf(node.asString());
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
                thingies[i++] = cnn.getNumber();
            }
            if(i >= 4) {
                break;
            }
        }
        return thingies;
    }

    private static ShaderProgram shaders(ConfigNode parent) {
        Option<ConfigArrayNode> arrNodeOpt = parent.getArrayOption(SHADERS_ARRAY_NAME);
        if (arrNodeOpt.isNone()) return null;
        ConfigArrayNode arrNode = arrNodeOpt.unwrap();
        ConfigNode[] values = arrNode.values();

        AssetHandle<Shader>[] shaders = new AssetHandle[values.length];
        Identifier[] idents = new Identifier[values.length];
        for (int i = 0; i < values.length; i++) {
            ConfigNode node = values[i];
            Identifier identifier = Identifier.of(node.getString(SHADERS_ARRAY_IDENTIFIER_NAME));
            shaders[i] = R.shaders.get(identifier);
            idents[i] = identifier;
        }
        return new ShaderProgram(shaders, idents);
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

    public static Iter<ConfigObjectNode> iterAttachments(AttachmentType type, ConfigNode parent) {
        Option<ConfigArrayNode> arrNodeOpt = parent.getArrayOption(ATTACHMENTS_ARRAY_NAME);
        if (arrNodeOpt.isNone()) return Iter.of();
        ConfigArrayNode arrNode = arrNodeOpt.unwrap();
        return Iter.of(Arrays.stream(arrNode.values()).map((node) -> new Pair<>(
                AttachmentType.valueOfOption(node.getStringOption(SHADERS_ARRAY_TYPE_NAME)
                        .unwrapOrPanic(new RuntimeException("Missing type in attachment definition!")))
                        .unwrapOrPanic(new RuntimeException("Unknown type in attachment definition!")),
                node.asObject()
                )))
                .filter((p) -> p.v1 == type)
                .map(p -> p.v2);
    }

    public static ArrayList<ColorAttachmentInfo> colorAttachments(ConfigNode parent) {
        ArrayList<ColorAttachmentInfo> attachments = new ArrayList<>();

        for (ConfigObjectNode node : iterAttachments(AttachmentType.COLOR, parent)) {
            attachments.add(new ColorAttachmentInfo(node));
        }

        return attachments;
    }

    public static Pair<DepthAttachmentInfo, StencilAttachmentInfo> getDepthStencilAttachments(ConfigNode parent) {
        Iter<ConfigObjectNode> depth = iterAttachments(AttachmentType.DEPTH, parent);
        Iter<ConfigObjectNode> stencil = iterAttachments(AttachmentType.STENCIL, parent);

        Option<ConfigObjectNode> d = depth.next();
        Option<ConfigObjectNode> s = stencil.next();

        if (depth.next().isSome()) throw new IllegalStateException("Cannot make multiple depth attachments!");
        if (stencil.next().isSome()) throw new IllegalStateException("Cannot make multiple depth attachments!");

        var p = new Pair<>(depthAttachment(d), stencilAttachment(s));

        if (p.v1 == null || p.v2 == null) return p;
        if (p.v1.format != p.v2.format) throw new IllegalStateException("If both Depth and Stencil attachments are present, the format must match!");

        return p;
    }

    private static DepthAttachmentInfo depthAttachment(Option<ConfigObjectNode> d) {
        return d.isSome() ? new DepthAttachmentInfo(d.unwrap()) : null;
    }

    private static StencilAttachmentInfo stencilAttachment(Option<ConfigObjectNode> s) {
        return s.isSome() ? new StencilAttachmentInfo(s.unwrap()) : null;
    }

    public static abstract class AttachmentInfo {
        public static final String
                TEXTURE_FORMAT_NAME = "format";

        public AttachmentType type;
        public Format format;

        public AttachmentInfo(ConfigObjectNode c, Format defaultFormat) {
            this.format = Format.valueOfOption(c.getString(TEXTURE_FORMAT_NAME)).unwrapOr(defaultFormat);
        }
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
                ALPHA_BLEND_OPERATION_NAME = "alphaBlendOperation";

        public int colorWriteMask = VK14.VK_COLOR_COMPONENT_R_BIT | VK14.VK_COLOR_COMPONENT_G_BIT | VK14.VK_COLOR_COMPONENT_B_BIT | VK14.VK_COLOR_COMPONENT_A_BIT;
        public boolean blendEnable = true;
        public BlendFactor srcBlendFactor = BlendFactor.SRC_ALPHA;
        public BlendFactor dstBlendFactor = BlendFactor.ONE_MINUS_SRC_ALPHA;
        public BlendFactor srcAlphaBlendFactor = BlendFactor.ONE;
        public BlendFactor dstAlphaBlendFactor = BlendFactor.ONE_MINUS_SRC_ALPHA;
        public BlendOperation colorBlendOperation = BlendOperation.ADD;
        public BlendOperation alphaBlendOperation = BlendOperation.ADD;

        public ColorAttachmentInfo(ConfigObjectNode c) {
            super(c, Format.BGRA8_SRGB);

            Option<ConfigArrayNode> colorWriteMaskArrayOpt = c.getArrayOption(COLOR_WRITE_MASK_ARRAY_NAME);
            if (colorWriteMaskArrayOpt.isSome())
                this.colorWriteMask = Arrays.stream(colorWriteMaskArrayOpt.unwrap().values())
                        .mapToInt((sth) -> (int) sth.asNumber()).reduce(0, (a, b) -> a | b);

            this.blendEnable = c.getBooleanOption(BLEND_ENABLE_NAME).unwrapOr(blendEnable);

            this.srcBlendFactor = BlendFactor.valueOfOption(c.getString(SRC_BLEND_FACTOR_NAME)).unwrapOr(srcBlendFactor);
            this.dstBlendFactor = BlendFactor.valueOfOption(c.getString(DST_BLEND_FACTOR_NAME)).unwrapOr(dstBlendFactor);
            this.srcAlphaBlendFactor = BlendFactor.valueOfOption(c.getString(SRC_ALPHA_BLEND_FACTOR_NAME)).unwrapOr(srcAlphaBlendFactor);
            this.dstAlphaBlendFactor = BlendFactor.valueOfOption(c.getString(DST_ALPHA_BLEND_FACTOR_NAME)).unwrapOr(dstAlphaBlendFactor);

            this.colorBlendOperation = BlendOperation.valueOfOption(c.getString(COLOR_BLEND_OPERATION_NAME)).unwrapOr(colorBlendOperation);
            this.alphaBlendOperation = BlendOperation.valueOfOption(c.getString(ALPHA_BLEND_OPERATION_NAME)).unwrapOr(alphaBlendOperation);
        }
    }

    public static class DepthAttachmentInfo extends AttachmentInfo {
        public static final String
                DEPTH_TEST_ENABLE_NAME = "depthTestEnable",
                DEPTH_WRITE_ENABLE_NAME = "depthWriteEnable",
                DEPTH_COMPARE_OP_NAME = "depthCompareOperation";

        public boolean depthTestEnable = true;
        public boolean depthWriteEnable = true;
        public CompareOp depthCompareOp = CompareOp.LEQUAL;

        public DepthAttachmentInfo(ConfigObjectNode c) {
            super(c, Format.DEPTH16);

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

        public boolean stencilTestEnable = false;
        public StencilOpState frontStencilOp = new StencilOpState();
        public StencilOpState backStencilOp = new StencilOpState();

        public StencilAttachmentInfo(ConfigObjectNode c) {
            super(c, Format.STENCIL8);

            this.stencilTestEnable = c.getBooleanOption(STENCIL_TEST_ENABLE_NAME).unwrapOr(stencilTestEnable);
            this.frontStencilOp = getOpState(frontStencilOp, c.getObject(FRONT_STENCIL_OP_NAME));
            this.backStencilOp = getOpState(backStencilOp, c.getObject(BACK_STENCIL_OP_NAME));
        }

        private static StencilOpState getOpState(StencilOpState defaultState, ConfigObjectNode c) {
            if (c == null) return defaultState;

            StencilOp failOp = StencilOp.valueOfOption(c.getString(FAIL_OP_NAME))
                    .unwrapOr(defaultState.failOp());
            StencilOp passOp = StencilOp.valueOfOption(c.getString(PASS_OP_NAME))
                    .unwrapOr(defaultState.passOp());
            StencilOp depthFailOp = StencilOp.valueOfOption(c.getString(DEPTH_FAIL_OP_NAME))
                    .unwrapOr(defaultState.depthFailOp());
            CompareOp compareOp = CompareOp.valueOfOption(c.getString(COMPARE_OP_NAME)).unwrapOr(defaultState.compareOp());
            int compareMask = c.getIntOption(COMPARE_MASK_NAME).unwrapOr(defaultState.compareMask());
            int writeMask = c.getIntOption(WRITE_MASK_NAME).unwrapOr(defaultState.writeMask());
            int reference = c.getIntOption(REFERENCE_NAME).unwrapOr(defaultState.reference());

            return new StencilOpState(failOp, passOp, depthFailOp, compareOp,
                                            compareMask, writeMask, reference);
        }
    }

    public enum AttachmentType {
        COLOR,
        DEPTH,
        STENCIL,
        DEPTH_STENCIL;

        public static Option<AttachmentType> valueOfOption(String name) {
            return Option.useIfNotFaulty(() -> AttachmentType.valueOf(name));
        }
    }

    public record StencilOpState(
            StencilOp failOp,
            StencilOp passOp,
            StencilOp depthFailOp,
            CompareOp compareOp,
            int compareMask,
            int writeMask,
            int reference
    ) {

        public StencilOpState() {
            this(
                    StencilOp.KEEP,
                    StencilOp.KEEP,
                    StencilOp.KEEP,
                    CompareOp.ALWAYS,
                    0xFF,
                    0xFF,
                    0
            );
        }

        public VkStencilOpState asVkObject(MemoryStack stack) {
            return VkStencilOpState.calloc(stack)
                    .failOp(this.failOp().getVkHandle())
                    .passOp(this.passOp().getVkHandle())
                    .depthFailOp(this.depthFailOp.getVkHandle())
                    .compareOp(this.compareOp.getVkHandle())
                    .compareMask(this.compareMask())
                    .writeMask(this.writeMask())
                    .reference(this.reference());
        }

    }

    public enum Topology implements IntEnum {

        POINTS(VK14.VK_PRIMITIVE_TOPOLOGY_POINT_LIST),
        PATCHES(VK14.VK_PRIMITIVE_TOPOLOGY_PATCH_LIST),
        LINES(VK14.VK_PRIMITIVE_TOPOLOGY_LINE_LIST),
        TRIANGLES(VK14.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST),

        LINES_ADJACENCY(VK14.VK_PRIMITIVE_TOPOLOGY_LINE_LIST_WITH_ADJACENCY),
        TRIANGLES_ADJACENCY(VK14.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST_WITH_ADJACENCY),

        LINE_STRIP(VK14.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP),
        TRIANGLE_STRIP(VK14.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP),

        LINE_STRIP_ADJACENCY(VK14.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP_WITH_ADJACENCY),
        TRIANGLE_STRIP_ADJACENCY(VK14.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP_WITH_ADJACENCY),

        TRIANGLE_FAN(VK14.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_FAN);

        private final int vkHandle;

        Topology(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return vkHandle;
        }
    }

    public enum PolygonMode implements IntEnum {

        POINT(VK14.VK_POLYGON_MODE_POINT),
        LINE(VK14.VK_POLYGON_MODE_LINE),
        FILL(VK14.VK_POLYGON_MODE_FILL);

        private final int vkHandle;

        PolygonMode(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return vkHandle;
        }

        public static Option<PolygonMode> valueOfOption(String name) {
            return Option.useIfNotFaulty(() -> PolygonMode.valueOf(name));
        }
    }

    public enum CullMode implements IntEnum {

        NONE(VK14.VK_CULL_MODE_NONE),
        FRONT(VK14.VK_CULL_MODE_FRONT_BIT),
        BACK(VK14.VK_CULL_MODE_BACK_BIT),
        FRONT_AND_BACK(VK14.VK_CULL_MODE_FRONT_AND_BACK);

        private final int vkHandle;

        CullMode(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return vkHandle;
        }

        public static Option<CullMode> valueOfOption(String name) {
            return Option.useIfNotFaulty(() -> CullMode.valueOf(name));
        }
    }

    public enum WindingOrder implements IntEnum {

        CLOCKWISE(VK14.VK_FRONT_FACE_CLOCKWISE),
        COUNTERCLOCKWISE(VK14.VK_FRONT_FACE_COUNTER_CLOCKWISE);

        private final int vkHandle;

        WindingOrder(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return vkHandle;
        }

        public static Option<WindingOrder> valueOfOption(String name) {
            return Option.useIfNotFaulty(() -> WindingOrder.valueOf(name));
        }
    }

    public enum BlendFactor implements IntEnum {

        ZERO(VK14.VK_BLEND_FACTOR_ZERO),
        ONE(VK14.VK_BLEND_FACTOR_ONE),
        ONE_MINUS_SRC(VK14.VK_BLEND_FACTOR_ONE_MINUS_SRC_COLOR),
        DST(VK14.VK_BLEND_FACTOR_DST_COLOR),
        ONE_MINUS_DST(VK14.VK_BLEND_FACTOR_ONE_MINUS_DST_COLOR),
        SRC_ALPHA(VK14.VK_BLEND_FACTOR_SRC_ALPHA),
        ONE_MINUS_SRC_ALPHA(VK14.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA),
        DST_ALPHA(VK14.VK_BLEND_FACTOR_DST_ALPHA),
        ONE_MINUS_DST_ALPHA(VK14.VK_BLEND_FACTOR_ONE_MINUS_DST_ALPHA),
        CONSTANT_COLOR(VK14.VK_BLEND_FACTOR_CONSTANT_COLOR),
        ONE_MINUS_CONSTANT_COLOR(VK14.VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_COLOR),
        CONSTANT_ALPHA(VK14.VK_BLEND_FACTOR_CONSTANT_ALPHA),
        ONE_MINUS_CONSTANT_ALPHA(VK14.VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_ALPHA),
        SRC_ALPHA_SATURATE(VK14.VK_BLEND_FACTOR_SRC_ALPHA_SATURATE),
        SRC1_COLOR(VK14.VK_BLEND_FACTOR_SRC1_COLOR),
        ONE_MINUS_SRC1_COLOR(VK14.VK_BLEND_FACTOR_ONE_MINUS_SRC1_COLOR),
        SRC1_ALPHA(VK14.VK_BLEND_FACTOR_SRC1_ALPHA),
        ONE_MINUS_SRC1_ALPHA(VK14.VK_BLEND_FACTOR_ONE_MINUS_SRC1_ALPHA);

        private final int vkHandle;

        BlendFactor(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return vkHandle;
        }

        public static Option<BlendFactor> valueOfOption(String name) {
            return Option.useIfNotFaulty(() -> BlendFactor.valueOf(name));
        }
    }

    public enum BlendOperation implements IntEnum {

        ADD(VK14.VK_BLEND_OP_ADD),
        SUBTRACT(VK14.VK_BLEND_OP_SUBTRACT),
        REVERSE_SUBTRACT(VK14.VK_BLEND_OP_REVERSE_SUBTRACT),
        MIN(VK14.VK_BLEND_OP_MIN),
        MAX(VK14.VK_BLEND_OP_MAX);

        private final int vkHandle;

        BlendOperation(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return vkHandle;
        }

        public static Option<BlendOperation> valueOfOption(String name) {
            return Option.useIfNotFaulty(() -> BlendOperation.valueOf(name));
        }
    }

    public enum StencilOp implements IntEnum {

        KEEP(VK14.VK_STENCIL_OP_KEEP),
        ZERO(VK14.VK_STENCIL_OP_ZERO),
        REPLACE(VK14.VK_STENCIL_OP_REPLACE),
        INCREMENT_AND_CLAMP(VK14.VK_STENCIL_OP_INCREMENT_AND_CLAMP),
        DECREMENT_AND_CLAMP(VK14.VK_STENCIL_OP_DECREMENT_AND_CLAMP),
        INVERT(VK14.VK_STENCIL_OP_INVERT),
        INCREMENT_AND_WRAP(VK14.VK_STENCIL_OP_INCREMENT_AND_WRAP),
        DECREMENT_AND_WRAP(VK14.VK_STENCIL_OP_DECREMENT_AND_WRAP);

        private final int vkHandle;

        StencilOp(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return vkHandle;
        }

        public static Option<StencilOp> valueOfOption(String name) {
            return Option.useIfNotFaulty(() -> StencilOp.valueOf(name));
        }
    }

    public enum DynamicState implements IntEnum {

        VIEWPORT(VK14.VK_DYNAMIC_STATE_VIEWPORT),
        SCISSOR(VK14.VK_DYNAMIC_STATE_SCISSOR),
        LINE_WIDTH(VK14.VK_DYNAMIC_STATE_LINE_WIDTH),
        DEPTH_BIAS(VK14.VK_DYNAMIC_STATE_DEPTH_BIAS),
        BLEND_CONSTANTS(VK14.VK_DYNAMIC_STATE_BLEND_CONSTANTS),
        @Deprecated
        DEPTH_BOUNDS(VK14.VK_DYNAMIC_STATE_DEPTH_BOUNDS),
        STENCIL_COMPARE_MASK(VK14.VK_DYNAMIC_STATE_STENCIL_COMPARE_MASK),
        STENCIL_WRITE_MASK(VK14.VK_DYNAMIC_STATE_STENCIL_WRITE_MASK),
        STENCIL_REFERENCE(VK14.VK_DYNAMIC_STATE_STENCIL_REFERENCE),

        DEPTH_WRITE(VK14.VK_DYNAMIC_STATE_DEPTH_WRITE_ENABLE);

        private final int vkHandle;

        DynamicState(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return vkHandle;
        }

        public static Option<DynamicState> valueOfOption(String name) {
            return Option.useIfNotFaulty(() -> DynamicState.valueOf(name));
        }
    }

}
