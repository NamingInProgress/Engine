package com.vke.core.rendering.vulkan.device;

import com.vke.api.vulkan.VulkanCreateInfo;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.stream.Collectors;

public class PhysicalDevice implements Disposable {

    private VkPhysicalDevice vk;
    private VkPhysicalDeviceProperties props;
    private VkExtensionProperties.Buffer extensionBuffer;
    private VkQueueFamilyProperties.Buffer queueFamilyBuffer;

    private AutoHeapAllocator alloc;

    public PhysicalDevice(VkPhysicalDevice vk) {
        this.vk = vk;
        this.alloc = new AutoHeapAllocator();

        props = alloc.allocStruct(VkPhysicalDeviceProperties.SIZEOF, VkPhysicalDeviceProperties::new);
        VK14.vkGetPhysicalDeviceProperties(vk, props);

        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pExtCount = stack.mallocInt(1);
            VK14.vkEnumerateDeviceExtensionProperties(vk, (ByteBuffer) null, pExtCount, null);

            extensionBuffer = alloc.allocBuffer(VkExtensionProperties.SIZEOF, pExtCount.get(0), VkExtensionProperties.Buffer::new);
            VK14.vkEnumerateDeviceExtensionProperties(vk, (ByteBuffer) null, pExtCount, extensionBuffer);


            IntBuffer count = stack.mallocInt(1);
            VK14.vkGetPhysicalDeviceQueueFamilyProperties(vk, count, null);

            queueFamilyBuffer = alloc.allocBuffer(VkQueueFamilyProperties.SIZEOF, count.get(0), VkQueueFamilyProperties.Buffer::new);
            VK14.vkGetPhysicalDeviceQueueFamilyProperties(vk, count, queueFamilyBuffer);
        }
    }

    public VkPhysicalDevice getDevice() {
        return this.vk;
    }

    public String getName() {
        return this.getProperties().deviceNameString();
    }

    public VkPhysicalDeviceProperties getProperties() {
        return this.props;
    }

    public VkExtensionProperties.Buffer getExtensionsBuffer() {
        return this.extensionBuffer;
    }

    public VkQueueFamilyProperties.Buffer getQueueFamilyBuffer() {
        return this.queueFamilyBuffer;
    }

    public List<VkQueueFamilyProperties> getRequiredProperties(VulkanCreateInfo vkCreateInfo) {
        int bits = vkCreateInfo.requiredQueueFamilyBits;
        return this.getQueueFamilyBuffer().stream().filter((props) ->
                (bits & props.queueFlags()) == bits).collect(Collectors.toList());
    }

    public VKCapabilitiesInstance getCapabilities() {
        return this.vk.getCapabilities();
    }

    @Override
    public void free() {
        alloc.close();
    }
}
