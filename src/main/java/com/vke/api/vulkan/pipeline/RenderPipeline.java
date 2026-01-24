package com.vke.api.vulkan.pipeline;

import com.carrotsearch.hppc.IntArrayList;
import com.vke.core.logger.LoggerFactory;
import org.lwjgl.vulkan.VK14;

import java.util.ArrayList;

public class RenderPipeline {

    private RenderPipeline() {

    }

    public static RenderPipelineBuilder builder() {
        return new RenderPipelineBuilder();
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
        int[] blendConstants = new int[]{ 0, 0, 0, 0 };

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

        public ColorAttachmentInfo setBlendConstants(int[] blendConstants) {
            if (blendConstants.length != 4) LoggerFactory.get("Render Pipeline Builder").warn("Tried to set blend constants with an array of length less that 4");
            this.blendConstants = blendConstants;
            return this;
        }

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
