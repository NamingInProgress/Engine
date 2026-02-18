package com.vke.core.vulkan.swapchain;

import com.vke.api.abstraction.IntEnum;
import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.data.TextureView;
import com.vke.api.abstraction.descriptors.texture.ImageAspect;
import com.vke.api.abstraction.descriptors.texture.TextureFormat;
import com.vke.api.abstraction.descriptors.texture.TextureViewType;
import com.vke.api.vulkan.ImageLayout;
import com.vke.core.vulkan.device.LogicalDevice;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.texture.VulkanTextureView;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

public class SwapchainImageView extends VulkanTextureView {

    public SwapchainImageView(SwapchainImage parent, LogicalDevice device, VkImageViewCreateInfo info) {
        super(device, parent,
                IntEnum.fromInt(TextureFormat.values(), info.format()),
                IntEnum.fromInt(TextureViewType.values(), info.viewType()),
                ImageAspect.fromMask(info.subresourceRange().aspectMask()),
                info.subresourceRange().baseMipLevel(),
                info.subresourceRange().baseArrayLayer(),
                info.subresourceRange().layerCount()
        );
    }

}
