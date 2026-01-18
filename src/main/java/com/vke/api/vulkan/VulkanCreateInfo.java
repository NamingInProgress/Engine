package com.vke.api.vulkan;

import com.vke.api.game.Version;
import org.lwjgl.vulkan.VK14;

import java.util.ArrayList;
import java.util.List;

public class VulkanCreateInfo {

    public final Version apiVersion = new Version(1, 4, 0);
    public final List<String> extensions = new ArrayList<>();
    public final List<String> gpuExtensions = new ArrayList<>();
    public final int requiredQueueFamilyBits = VK14.VK_QUEUE_GRAPHICS_BIT | VK14.VK_QUEUE_COMPUTE_BIT;

}
