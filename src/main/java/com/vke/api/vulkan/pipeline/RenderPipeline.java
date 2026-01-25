package com.vke.api.vulkan.pipeline;

import com.carrotsearch.hppc.IntArrayList;
import com.vke.api.logger.LogLevel;
import com.vke.api.logger.Logger;
import com.vke.api.vulkan.shaders.ShaderProgram;
import com.vke.core.logger.LoggerFactory;
import org.lwjgl.vulkan.VK14;

import java.util.ArrayList;

public class RenderPipeline {

    private static PipelineVerbosity verbosity = PipelineVerbosity.WARN;
    private static final Logger LOG = LoggerFactory.get("RenderPipeline");

    private RenderPipeline(RenderPipelineBuilder builder) {
        // TODO: Add actually setting up the vk structs
    }

    public static RenderPipelineBuilder builder() {
        return new RenderPipelineBuilder();
    }

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

    public static final class RenderPipelineBuilder {

        // Dynamic State
        IntArrayList dynamicStates = IntArrayList.from(VK14.VK_DYNAMIC_STATE_VIEWPORT, VK14.VK_DYNAMIC_STATE_SCISSOR);

        // Input Assembly
        boolean primitiveRestartEnable = false;
        Topology topology;

        // Raster Info
        PolygonMode polygonMode = PolygonMode.FILL;
        CullMode cullMode = CullMode.FRONT;
        FrontFace frontFace = FrontFace.COUNTERCLOCKWISE;
        float lineWidth = 1.0f;
        boolean depthBiasEnable = false;
        float depthBiasConstFactor = 0.0f;
        float depthBiasClamp = 0.0f;
        float depthBiasSlopeFactor = 0.0f;

        // Attachments
        ArrayList<ColorAttachmentInfo> colorAttachments = new ArrayList<>();
        boolean depthAttachment = false;
        boolean stencilAttachment = false;
        int[] blendConstants = new int[]{ 0, 0, 0, 0 };

        // Shader
        ShaderProgram shader;

        public RenderPipeline build() {
            return new RenderPipeline(this);
        }

        public RenderPipelineBuilder addDynamicState(int state) {
            this.dynamicStates.add(state);
            return this;
        }

        public RenderPipelineBuilder withColorAttachment(ColorAttachmentInfo info) {
            this.colorAttachments.add(info);
            if (this.colorAttachments.size() > 4) log(LogLevel.TRACE, "Color attachments amount passes minimal provided level of 4!");
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

        public RenderPipelineBuilder setTopology(Topology topology) {
            this.topology = topology;
            return this;
        }

        public RenderPipelineBuilder setPolygonMode(PolygonMode polygonMode) {
            this.polygonMode = polygonMode;
            return this;
        }

        public RenderPipelineBuilder setCullMode(CullMode cullMode) {
            this.cullMode = cullMode;
            return this;
        }

        public RenderPipelineBuilder setLineWidth(float lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        public RenderPipelineBuilder setFrontFace(FrontFace frontFace) {
            this.frontFace = frontFace;
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

        public RenderPipelineBuilder withDepthAttachment(boolean depthAttachment) {
            this.depthAttachment = depthAttachment;
            return this;
        }

        public RenderPipelineBuilder withStencilAttachment(boolean stencilAttachment) {
            this.stencilAttachment = stencilAttachment;
            return this;
        }

        public RenderPipelineBuilder setBlendConstants(int[] blendConstants) {
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



    }

    public static enum PipelineVerbosity {
        ALL,
        WARN,
        ERROR
    }

    private interface VkEnum {
        int getVkHandle();
    }

    public static enum Topology implements VkEnum {

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

    public static enum PolygonMode implements VkEnum {

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

    public static enum CullMode implements VkEnum {

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

    public static enum FrontFace implements VkEnum {

        CLOCKWISE(VK14.VK_FRONT_FACE_CLOCKWISE),
        COUNTERCLOCKWISE(VK14.VK_FRONT_FACE_COUNTER_CLOCKWISE);

        private final int vkHandle;

        FrontFace(int vkHandle) {
            this.vkHandle = vkHandle;
        }

        @Override
        public int getVkHandle() {
            return vkHandle;
        }
    }

    public static enum BlendFactor implements VkEnum {

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

    public static enum BlendOperation implements VkEnum {

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

}
