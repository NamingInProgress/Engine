package com.vke.api.vulkan.createInfos;

import com.vke.api.game.Version;
import com.vke.core.rendering.vulkan.Consts;
import com.vke.utils.ObservableList;
import org.lwjgl.vulkan.VK14;

import java.util.ArrayList;
import java.util.List;

public class VulkanCreateInfo {

    public int framesInFlight = 2;
    public Version apiVersion = new Version(1, 4, 0);
    public List<String> extensions = new ArrayList<>();
    public List<String> gpuExtensions = new ArrayList<>(List.of(
            Consts.KHR_SWAPCHAIN_EXTENSION,
            Consts.KHR_SPIRV_EXTENSION,
            Consts.KHR_SYNCHRONIZATION_2,
            Consts.KHR_CREATE_RENDERPASS_2
    ));

    public ObservableList<List<Integer>, Integer> requiredQueueFamilyBitsList = new ObservableList<>(List.of(
            VK14.VK_QUEUE_GRAPHICS_BIT, VK14.VK_QUEUE_COMPUTE_BIT
    )) {{
        observe(() -> {
            requiredQueueFamilyBits = recompute(this);
        });
    }};
    public int requiredQueueFamilyBits = recompute(requiredQueueFamilyBitsList);

    private static int or(int a, int b) {
        return a | b;
    }
    private static int recompute(List<Integer> bits) {
        return bits
                .stream()
                .reduce(0, VulkanCreateInfo::or);
    }
}
