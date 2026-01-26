package com.vke.api.vulkan.pipeline;

import com.vke.api.logger.LogLevel;
import com.vke.api.logger.Logger;
import com.vke.api.registry.VKERegistries;
import com.vke.api.registry.builders.VKERegistrar;
import com.vke.api.vulkan.createInfos.PipelineCreateInfo;
import com.vke.api.vulkan.shaders.ShaderProgram;
import com.vke.core.VKEngine;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.rendering.vulkan.pipeline.GraphicsPipeline;
import com.vke.core.rendering.vulkan.shader.Shader;
import com.vke.core.rendering.vulkan.shader.VKShaderProgram;
import com.vke.utils.Disposable;
import com.vke.utils.Identifier;
import com.vke.utils.Utils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkStencilOpState;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class RenderPipeline implements Disposable {

    private final RenderPipelineBuilder builder;

    private GraphicsPipeline graphicsPipeline;

    private RenderPipeline(RenderPipelineBuilder builder) {
        this.builder = builder;
    }

    public void setupGraphicsPipeline(VKEngine engine, VulkanSetup vkSetup) {
        if (graphicsPipeline != null) {
            log(LogLevel.WARN, "Remaking graphics pipeline from RenderPipeline, is this a bug?");
        }

        PipelineCreateInfo pipelineCreateInfo = new PipelineCreateInfo();
        pipelineCreateInfo.device = vkSetup.getLogicalDevice();
        pipelineCreateInfo.engine = engine;
        pipelineCreateInfo.swapChain = vkSetup.getSwapChain();
        pipelineCreateInfo.name = builder.getId();

        int depthFormat = VK14.VK_FORMAT_UNDEFINED;
        int stencilFormat = VK14.VK_FORMAT_UNDEFINED;

        if (builder.depthStencilAttachment != null) {
            if (builder.autoRegisterDynamicStates)
                builder.dynamicStates.add(DynamicState.DEPTH_WRITE);
            if (builder.stencilAttachment) {
                if (builder.autoRegisterDynamicStates) {
                    builder.dynamicStates.addAll(List.of(DynamicState.STENCIL_COMPARE_MASK,
                            DynamicState.STENCIL_WRITE_MASK,
                            DynamicState.STENCIL_REFERENCE));
                }
                depthFormat = VK14.VK_FORMAT_D32_SFLOAT_S8_UINT;
                stencilFormat = depthFormat;
            } else {
                depthFormat = VK14.VK_FORMAT_D32_SFLOAT;
            }
        } else {
            if (builder.stencilAttachment) {
                engine.throwException(new IllegalStateException("Tried to make a pipeline with a stencil attachment but no depth attachment!"), "Render Pipeline");
            }
        }

        Shader[] shaders = new Shader[0];
        try {
            shaders = builder.shader.getShaderArray(engine, vkSetup.getLogicalDevice(), engine.getCompiler());
        } catch (Exception e) {
            engine.throwException(e, "Render Pipeline -> Shader Creation");
        }
        VKShaderProgram shader = new VKShaderProgram(shaders);

        GraphicsPipeline.PipelineSettingsInfo pipelineSettingsInfo =
                new GraphicsPipeline.PipelineSettingsInfo(
                        // Dynamic State
                        Utils.asIntArray(builder.dynamicStates, DynamicState::getVkHandle),

                        // Input Assembly
                        builder.primitiveRestartEnable,
                        builder.topology.getVkHandle(),

                        // Raster Info
                        builder.polygonMode.getVkHandle(),
                        builder.cullMode.getVkHandle(),
                        builder.windingOrder.getVkHandle(),
                        builder.lineWidth,
                        builder.depthBiasEnable,
                        builder.depthBiasConstFactor,
                        builder.depthBiasClamp,
                        builder.depthBiasSlopeFactor,

                        // Attachments
                        builder.colorAttachments,
                        builder.depthStencilAttachment,
                        depthFormat,
                        stencilFormat,
                        builder.blendConstants,

                        // Shaders
                        shader
                );

        graphicsPipeline = new GraphicsPipeline(pipelineCreateInfo, pipelineSettingsInfo);
    }

    public GraphicsPipeline getGraphicsPipeline() {
        if (graphicsPipeline == null) {
            log(LogLevel.FATAL, "Tried to access internal graphics pipeline without calling RenderPipeline#setupGraphicsPipeline first!");
            log(LogLevel.FATAL, "RenderPipeline.graphicsPipeline is null! Was the pipeline registered?");
            throw new IllegalStateException("GraphicsPipeline is null! RenderPipeline#getGraphicsPipeline");
        }
        return graphicsPipeline;
    }

    public Set<DynamicState> dynamicStates() {
        if (graphicsPipeline == null) log(LogLevel.WARN, "Accessed dynamic states while graphics pipeline was not built yet which can change the dynamic states enabled!");
        return Set.copyOf(builder.dynamicStates);
    }

    @Override
    public void free() {
        graphicsPipeline.free();
    }

    /**  Logger  **/
    private static PipelineVerbosity verbosity = PipelineVerbosity.WARN;
    private static final Logger LOG = LoggerFactory.get("RenderPipeline");

    public static void setPipelineVerbosity(PipelineVerbosity verbosity) {
        RenderPipeline.verbosity = verbosity;
    }

    static void log(LogLevel level, String message) {
        switch (verbosity) {
            case ALL -> LOG.log(level, message);
            case WARN -> {
                if (level.ordinal() > LogLevel.INFO.ordinal()) LOG.log(level, message);
            }
            case ERROR -> {
                if (level.ordinal() > LogLevel.WARN.ordinal()) LOG.log(level, message);
            }
        }
    }

    public static final class RenderPipelineBuilder extends VKERegistrar<Identifier, RenderPipeline> {

        // Dynamic State
        ArrayList<DynamicState> dynamicStates = new ArrayList<>(List.of(DynamicState.VIEWPORT, DynamicState.SCISSOR));

        // Input Assembly
        boolean primitiveRestartEnable = false;
        Topology topology;

        // Raster Info
        PolygonMode polygonMode = PolygonMode.FILL;
        CullMode cullMode = CullMode.BACK;
        WindingOrder windingOrder = WindingOrder.COUNTERCLOCKWISE;
        float lineWidth = 1.0f;
        boolean depthBiasEnable = false;
        float depthBiasConstFactor = 0.0f;
        float depthBiasClamp = 0.0f;
        float depthBiasSlopeFactor = 0.0f;

        // Attachments
        ArrayList<ColorAttachmentInfo> colorAttachments = new ArrayList<>();
        DepthStencilAttachmentInfo depthStencilAttachment = null;
        boolean stencilAttachment = false;
        float[] blendConstants = new float[]{ 0, 0, 0, 0 };
        boolean autoRegisterDynamicStates = false;

        // Shader
        ShaderProgram shader;

        public RenderPipelineBuilder(Identifier key) {
            super(key);
        }

        @Override
        protected void addToRegistry(Identifier key, RenderPipeline value) {
            VKERegistries.PIPELINES.register(key, value);
        }

        @Override
        protected RenderPipeline build() {
            return new RenderPipeline(this);
        }

        protected Identifier getId() { return this.key; }

        public RenderPipelineBuilder addDynamicState(DynamicState state) {
            this.dynamicStates.add(state);
            return this;
        }

        public RenderPipelineBuilder setRegisterDynamicStates(boolean a) {
            this.autoRegisterDynamicStates = a;
            return this;
        }

        public RenderPipelineBuilder withColorAttachment(ColorAttachmentInfo info) {
            this.colorAttachments.add(info);
            return this;
        }

        public RenderPipelineBuilder withShader(ShaderProgram shader) {
            this.shader = shader;
            return this;
        }

        public RenderPipelineBuilder setPrimitiveRestartEnable(boolean primitiveRestartEnable) {
            this.primitiveRestartEnable = primitiveRestartEnable;
            return this;
        }

        public RenderPipelineBuilder topology(Topology topology) {
            this.topology = topology;
            return this;
        }

        public RenderPipelineBuilder setPolygonMode(PolygonMode polygonMode) {
            this.polygonMode = polygonMode;
            return this;
        }

        public RenderPipelineBuilder cullMode(CullMode cullMode) {
            this.cullMode = cullMode;
            return this;
        }

        public RenderPipelineBuilder setLineWidth(float lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        public RenderPipelineBuilder windingOrder(WindingOrder windingOrder) {
            this.windingOrder = windingOrder;
            return this;
        }

        public RenderPipelineBuilder setDepthBiasEnable(boolean depthBiasEnable) {
            this.depthBiasEnable = depthBiasEnable;
            return this;
        }

        public RenderPipelineBuilder setDepthBiasConstFactor(float depthBiasConstFactor) {
            this.depthBiasConstFactor = depthBiasConstFactor;
            return this;
        }

        public RenderPipelineBuilder setDepthBiasClamp(float depthBiasClamp) {
            this.depthBiasClamp = depthBiasClamp;
            return this;
        }

        public RenderPipelineBuilder setDepthBiasSlopeFactor(float depthBiasSlopeFactor) {
            this.depthBiasSlopeFactor = depthBiasSlopeFactor;
            return this;
        }

        public RenderPipelineBuilder withDepthAttachment(DepthStencilAttachmentInfo info) {
            this.depthStencilAttachment = info;
            return this;
        }

        public RenderPipelineBuilder withStencilAttachment(boolean stencilAttachment) {
            this.stencilAttachment = stencilAttachment;
            return this;
        }

        public RenderPipelineBuilder setBlendConstants(float[] blendConstants) {
            if (blendConstants.length != 4) log(LogLevel.WARN, "Tried to set blend constants with an array of length less that 4");
            this.blendConstants = blendConstants;
            return this;
        }
    }

    public static class ColorAttachmentInfo {

        int colorWriteMask = VK14.VK_COLOR_COMPONENT_R_BIT | VK14.VK_COLOR_COMPONENT_G_BIT | VK14.VK_COLOR_COMPONENT_B_BIT | VK14.VK_COLOR_COMPONENT_A_BIT;
        boolean blendEnable = true;
        BlendFactor srcBlendFactor = BlendFactor.SRC_ALPHA;
        BlendFactor dstBlendFactor = BlendFactor.ONE_MINUS_SRC_ALPHA;
        BlendFactor srcAlphaBlendFactor = BlendFactor.ONE;
        BlendFactor dstAlphaBlendFactor = BlendFactor.ONE_MINUS_SRC_ALPHA;
        BlendOperation colorBlendOperation = BlendOperation.ADD;
        BlendOperation alphaBlendOperation = BlendOperation.ADD;
        int format = VK14.VK_FORMAT_B8G8R8A8_SRGB;

        public ColorAttachmentInfo setColorWriteMask(int colorWriteMask) {
            this.colorWriteMask = colorWriteMask;
            return this;
        }

        public ColorAttachmentInfo enableBlend(boolean blendEnable) {
            this.blendEnable = blendEnable;
            return this;
        }

        public ColorAttachmentInfo srcBlendFactor(BlendFactor srcBlendFactor) {
            this.srcBlendFactor = srcBlendFactor;
            return this;
        }

        public ColorAttachmentInfo dstBlendFactor(BlendFactor dstBlendFactor) {
            this.dstBlendFactor = dstBlendFactor;
            return this;
        }

        public ColorAttachmentInfo srcAlphaBlendFactor(BlendFactor srcAlphaBlendFactor) {
            this.srcAlphaBlendFactor = srcAlphaBlendFactor;
            return this;
        }

        public ColorAttachmentInfo dstAlphaBlendFactor(BlendFactor dstAlphaBlendFactor) {
            this.dstAlphaBlendFactor = dstAlphaBlendFactor;
            return this;
        }

        public ColorAttachmentInfo colorBlendOp(BlendOperation colorBlendOperation) {
            this.colorBlendOperation = colorBlendOperation;
            return this;
        }

        public ColorAttachmentInfo alphaBlendOp(BlendOperation alphaBlendOperation) {
            this.alphaBlendOperation = alphaBlendOperation;
            return this;
        }

        public ColorAttachmentInfo format(int format) {
            this.format = format;
            return this;
        }

        public int getColorWriteMask() {
            return colorWriteMask;
        }

        public boolean isBlendEnable() {
            return blendEnable;
        }

        public BlendFactor getSrcBlendFactor() {
            return srcBlendFactor;
        }

        public BlendFactor getDstBlendFactor() {
            return dstBlendFactor;
        }

        public BlendFactor getSrcAlphaBlendFactor() {
            return srcAlphaBlendFactor;
        }

        public BlendFactor getDstAlphaBlendFactor() {
            return dstAlphaBlendFactor;
        }

        public BlendOperation getColorBlendOperation() {
            return colorBlendOperation;
        }

        public BlendOperation getAlphaBlendOperation() {
            return alphaBlendOperation;
        }

        public int getFormat() {
            return format;
        }
    }

    public static class DepthStencilAttachmentInfo {

        boolean depthTestEnable = true;
        boolean depthWriteEnable = true;
        CompareOp depthCompareOp = CompareOp.LEQUAL;
        boolean stencilTestEnable = false;
        StencilOpState frontStencilOp = new StencilOpState();
        StencilOpState backStencilOp = new StencilOpState();

        public DepthStencilAttachmentInfo setDepthTestEnable(boolean depthTestEnable) {
            this.depthTestEnable = depthTestEnable;
            return this;
        }

        public DepthStencilAttachmentInfo setDepthWriteEnable(boolean depthWriteEnable) {
            this.depthWriteEnable = depthWriteEnable;
            return this;
        }

        public DepthStencilAttachmentInfo setDepthCompareOp(CompareOp depthCompareOp) {
            this.depthCompareOp = depthCompareOp;
            return this;
        }

        public DepthStencilAttachmentInfo setStencilTestEnable(boolean stencilTestEnable) {
            this.stencilTestEnable = stencilTestEnable;
            return this;
        }

        public DepthStencilAttachmentInfo setFrontStencilOp(StencilOpState frontStencilOp) {
            this.frontStencilOp = frontStencilOp;
            return this;
        }

        public DepthStencilAttachmentInfo setBackStencilOp(StencilOpState backStencilOp) {
            this.backStencilOp = backStencilOp;
            return this;
        }

        public boolean isDepthTestEnable() {
            return depthTestEnable;
        }

        public boolean isDepthWriteEnable() {
            return depthWriteEnable;
        }

        public CompareOp getDepthCompareOp() {
            return depthCompareOp;
        }

        public boolean isStencilTestEnable() {
            return stencilTestEnable;
        }

        public StencilOpState getFrontStencilOp() {
            return frontStencilOp;
        }

        public StencilOpState getBackStencilOp() {
            return backStencilOp;
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

    public enum PipelineVerbosity {
        ALL,
        WARN,
        ERROR
    }

    private interface VkEnum {
        int getVkHandle();
    }

    public enum Topology implements VkEnum {

        POINTS(VK14.VK_PRIMITIVE_TOPOLOGY_POINT_LIST),
        PATCHES(VK14.VK_PRIMITIVE_TOPOLOGY_PATCH_LIST),
        LINES(VK14.VK_PRIMITIVE_TOPOLOGY_LINE_LIST),
        TRIANGLES(VK14.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST),

        LINES_ADJACENCY(VK14.VK_PRIMITIVE_TOPOLOGY_LINE_LIST_WITH_ADJACENCY),
        TRIANGLES_ADJACENCY(VK14.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST_WITH_ADJACENCY),

        LINES_STRIP(VK14.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP),
        TRIANGLES_STRIP(VK14.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP),

        LINES_STRIP_ADJACENCY(VK14.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP_WITH_ADJACENCY),
        TRIANGLES_STRIP_ADJACENCY(VK14.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP_WITH_ADJACENCY),

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

    public enum PolygonMode implements VkEnum {

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
    }

    public enum CullMode implements VkEnum {

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
    }

    public enum WindingOrder implements VkEnum {

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
    }

    public enum BlendFactor implements VkEnum {

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
    }

    public enum BlendOperation implements VkEnum {

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
    }

    public enum CompareOp implements VkEnum {

        NEVER(VK14.VK_COMPARE_OP_NEVER),
        LESS(VK14.VK_COMPARE_OP_LESS),
        EQUAL(VK14.VK_COMPARE_OP_EQUAL),
        LEQUAL(VK14.VK_COMPARE_OP_LESS_OR_EQUAL),
        GREATER(VK14.VK_COMPARE_OP_GREATER),
        NOT_EQUAL(VK14.VK_COMPARE_OP_NOT_EQUAL),
        GEQUAL(VK14.VK_COMPARE_OP_GREATER_OR_EQUAL),
        ALWAYS(VK14.VK_COMPARE_OP_ALWAYS);

        private final int vkHandle;

        CompareOp(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return vkHandle;
        }
    }

    public enum StencilOp implements VkEnum {

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
    }

    public enum DynamicState implements VkEnum {

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
    }

}
