package com.vke.api.rendering.vulkan;

import org.lwjgl.vulkan.VK14;

public enum ImageState {

    UNDEFINED(
            ImageLayout.UNDEFINED,
            VK14.VK_PIPELINE_STAGE_2_NONE,
            VK14.VK_ACCESS_2_NONE
    ),

    TRANSFER_DST(
            ImageLayout.TRANSFER_DST_OPTIMAL,
            VK14.VK_PIPELINE_STAGE_2_ALL_TRANSFER_BIT,
            VK14.VK_ACCESS_2_TRANSFER_WRITE_BIT
    ),

    TRANSFER_SRC(
            ImageLayout.TRANSFER_SRC_OPTIMAL,
            VK14.VK_PIPELINE_STAGE_2_ALL_TRANSFER_BIT,
            VK14.VK_ACCESS_2_TRANSFER_READ_BIT
    ),

    COLOR_ATTACHMENT(
            ImageLayout.COLOR_ATTACHMENT_OPTIMAL,
            VK14.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
            VK14.VK_ACCESS_2_COLOR_ATTACHMENT_READ_BIT |
                    VK14.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT
    ),

    DEPTH_STENCIL_ATTACHMENT(
            ImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
            VK14.VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT |
                    VK14.VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT,
            VK14.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_READ_BIT |
                    VK14.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT
    ),

    FRAGMENT_SHADER_READ(
            ImageLayout.SHADER_READONLY_OPTIMAL,
            VK14.VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT,
            VK14.VK_ACCESS_2_SHADER_SAMPLED_READ_BIT
    ),

    GENERAL_SHADER_READ(
            ImageLayout.SHADER_READONLY_OPTIMAL,
            VK14.VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT,
            VK14.VK_ACCESS_2_SHADER_SAMPLED_READ_BIT
    ),

    COMPUTE_SHADER_READ(
            ImageLayout.SHADER_READONLY_OPTIMAL,
            VK14.VK_PIPELINE_STAGE_2_COMPUTE_SHADER_BIT,
            VK14.VK_ACCESS_2_SHADER_SAMPLED_READ_BIT
    ),

    PRESENT(
            ImageLayout.PRESENT_SRC_KHR,
            VK14.VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT,
            VK14.VK_ACCESS_2_NONE
    );

    private final ImageLayout layout;
    private final long stageMask, accessMask;

    ImageState(ImageLayout layout, long stageMask, long accessMask) {
        this.layout = layout;
        this.stageMask = stageMask;
        this.accessMask = accessMask;
    }

    public ImageLayout getLayout() {
        return layout;
    }

    public int getLayoutHandle() {
        return layout.getVkHandle();
    }

    public long getStageMask() {
        return stageMask;
    }

    public long getAccessMask() {
        return accessMask;
    }
}
