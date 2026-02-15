package com.vke.core.vulkan.swapchain;

import com.vke.api.abstraction.IntEnum;
import com.vke.api.abstraction.RenderDevice;
import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.descriptors.BackendType;
import com.vke.api.abstraction.descriptors.texture.TextureFormat;
import com.vke.api.abstraction.swapchain.Swapchain;
import com.vke.api.abstraction.sync.Semaphore;
import com.vke.core.VKEngine;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.memory.intP;
import com.vke.core.vulkan.device.LogicalDevice;
import com.vke.api.abstraction.descriptors.QueueType;
import com.vke.core.vulkan.device.VulkanQueue;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.sync.VulkanSemaphore;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;

public class VulkanSwapchain implements Swapchain {

    private static final String HERE = "Swapchain@VulkanImpl";

    private long swapchain;

    private VkSurfaceCapabilitiesKHR capabilities;
    private VkSurfaceFormatKHR.Buffer formats;
    private IntBuffer modes;

    private VkExtent2D extent;

    private TextureFormat format;
    private boolean vsync;
    private long surface;

    private final ArrayList<SwapchainImage> images = new ArrayList<>();
    private final ArrayList<SwapchainImageView> imageViews = new ArrayList<>();

    private int currentImageIndex;

    // Engine Infos
    private final VKEngine engine;
    private final VulkanRenderDevice device;
    private final long windowHandle;

    private final AutoHeapAllocator alloc;

    public VulkanSwapchain(Description description, RenderDevice device, VKEngine engine) {
        if (!(device instanceof VulkanRenderDevice)) engine
                .throwException(new IllegalStateException("Provided incorrect device for backend type: %s (Provided: %s)".formatted(BackendType.VULKAN, device.backend())), HERE);

        this.device = (VulkanRenderDevice) device;
        this.engine = engine;
        this.vsync = description.vsync();
        this.windowHandle = description.windowHandle();
        this.surface = this.device.getSurface();
        this.alloc = new AutoHeapAllocator();

        setupInfoStructs(this.device.getPhysicalDevice().getDevice(), this.device.getSurface());
        createSwapchain(this.device.getLogicalDevice());
    }

    private void setupInfoStructs(VkPhysicalDevice device, long surface) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSurfaceCapabilitiesKHR pCap = alloc.allocStruct(VkSurfaceCapabilitiesKHR.SIZEOF, VkSurfaceCapabilitiesKHR::new);

            KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, pCap);

            IntBuffer pFormatCount = stack.mallocInt(1);
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, pFormatCount, null);
            int formatCount = pFormatCount.get(0);
            VkSurfaceFormatKHR.Buffer pFormat = alloc.allocBuffer(VkSurfaceFormatKHR.SIZEOF, formatCount, VkSurfaceFormatKHR.Buffer::new);
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, pFormatCount, pFormat);

            IntBuffer pPresentCount = stack.mallocInt(1);
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, pPresentCount, null);
            int presentCount = pPresentCount.get(0);
            intP pModesHeap = alloc.allocInt(presentCount);
            IntBuffer pModes = pModesHeap.getHeapObject();
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, pPresentCount, pModes);

            this.capabilities = pCap;
            this.formats = pFormat;
            this.modes = pModes;
        }
    }

    private VkSwapchainCreateInfoKHR getCreateInfo(MemoryStack stack) {
        VkSwapchainCreateInfoKHR info = VkSwapchainCreateInfoKHR.calloc(stack);

        VkSurfaceFormatKHR pickedFormat = SwapchainUtils.chooseFormat(formats);
        int presentMode = SwapchainUtils.choosePresentMode(modes, vsync);
        VkExtent2D extent2D = SwapchainUtils.chooseExtent(capabilities, alloc, windowHandle);
        int minImageCount = Math.max(3, capabilities.minImageCount());
        minImageCount = ( capabilities.maxImageCount() > 0 && minImageCount > capabilities.maxImageCount() ) ? capabilities.maxImageCount() : minImageCount;

        info.sType$Default()
                .surface(surface)
                .minImageCount(minImageCount)
                .imageFormat(pickedFormat.format())
                .imageColorSpace(pickedFormat.colorSpace())
                .imageExtent(extent2D)
                .imageArrayLayers(1)
                .imageUsage(VK14.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                .preTransform(capabilities.currentTransform())
                .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                .presentMode(presentMode)
                .oldSwapchain(VK14.VK_NULL_HANDLE)
                .clipped(true);

        VulkanQueue graphicsQueue = device.getQueue(QueueType.GRAPHICS);
        VulkanQueue presentQueue = device.getQueue(QueueType.PRESENT);

        if (!graphicsQueue.equals(presentQueue)) {
            IntBuffer queueIndices = stack.ints(graphicsQueue.index(), presentQueue.index()); // replaced alloc!

            info.imageSharingMode(VK14.VK_SHARING_MODE_CONCURRENT); // TODO: Replace with exclusive with memory transfers
            info.queueFamilyIndexCount(2);
            info.pQueueFamilyIndices(queueIndices);
        } else {
            IntBuffer queueIndex = alloc.ints(graphicsQueue.index()).getHeapObject();

            info.imageSharingMode(VK14.VK_SHARING_MODE_EXCLUSIVE);
            info.queueFamilyIndexCount(1);
            info.pQueueFamilyIndices(queueIndex);
        }

        this.format = IntEnum.fromInt(TextureFormat.values(), pickedFormat.format());
        this.extent = extent2D;

        return info;
    }

    private void createSwapchain(LogicalDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pSwapChain = stack.callocLong(1);
            VkSwapchainCreateInfoKHR createInfo = getCreateInfo(stack);

            if (KHRSwapchain.vkCreateSwapchainKHR(device.getDevice(), createInfo, null, pSwapChain) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create Swap Chain!"), HERE);
            }

            this.swapchain = pSwapChain.get(0);

            createImages(stack, device);
        }
    }

    private void createImages(MemoryStack stack, LogicalDevice device) {
        IntBuffer count = stack.mallocInt(1);
        KHRSwapchain.vkGetSwapchainImagesKHR(device.getDevice(), swapchain, count, null);

        LongBuffer images = alloc.allocLong(count.get(0)).getHeapObject();
        KHRSwapchain.vkGetSwapchainImagesKHR(device.getDevice(), swapchain, count, images);

        for (int i = 0; i < count.get(0); i++) {
            this.images.add(new SwapchainImage(images.get(i), this.format));
        }

        VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc(stack)
                .aspectMask(VK14.VK_IMAGE_ASPECT_COLOR_BIT)
                .baseMipLevel(0)
                .levelCount(1)
                .baseArrayLayer(0)
                .layerCount(1);

        VkImageViewCreateInfo baseInfo = VkImageViewCreateInfo.calloc(stack)
                .viewType(VK14.VK_IMAGE_VIEW_TYPE_2D)
                .format(format.getVkHandle())
                .subresourceRange(subresourceRange)
                .sType$Default();

        for (int i = 0; i < count.get(0); i++) {
            SwapchainImage image = this.images.get(i);
            VkImageViewCreateInfo info = SwapchainUtils.copyImageViewCreateInfo(baseInfo);
            info.image(image.getHandle());
            try {
                SwapchainImageView view = new SwapchainImageView(image, device, info);
                this.imageViews.add(view);
            } catch (Throwable t) {
                engine.throwException(t, "SwapchainImageView@VulkanImpl");
            }
        }
    }

    @Override
    public int width() {
        return extent.width();
    }

    @Override
    public int height() {
        return extent.height();
    }

    @Override
    public TextureFormat format() {
        return this.format;
    }

    @Override
    public int acquireNextImage(Semaphore imageAvailable) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkAcquireNextImageInfoKHR acquireInfo = VkAcquireNextImageInfoKHR.calloc(stack);
            acquireInfo
                    .deviceMask(1)
                    .swapchain(swapchain)
                    .fence(VK14.VK_NULL_HANDLE)
                    .semaphore(((VulkanSemaphore) imageAvailable).getHandle())
                    .sType$Default();
            IntBuffer pNextImageIndex = stack.mallocInt(1);
            int VK_RESULT = KHRSwapchain.vkAcquireNextImage2KHR(device.getLogicalDevice().getDevice(), acquireInfo, pNextImageIndex);
            if (VK_RESULT != VK14.VK_SUCCESS) {
                return ~VK_RESULT;
            }
            currentImageIndex = pNextImageIndex.get(0);
            return currentImageIndex;
        }
    }

    @Override
    public Texture getImage(int index) {
        return images.get(index);
    }

    public SwapchainImageView getImageView(int index) {
        return imageViews.get(index);
    }

    @Override
    public void present(Semaphore renderFinished) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
            presentInfo.sType$Default();
            presentInfo.pImageIndices(stack.ints(currentImageIndex));
            presentInfo.pSwapchains(stack.longs(swapchain));
            presentInfo.pWaitSemaphores(stack.longs(((VulkanSemaphore) renderFinished).getHandle()));
            presentInfo.swapchainCount(1);

            int VK_RESULT = KHRSwapchain.vkQueuePresentKHR(device.getQueue(QueueType.PRESENT).vk(), presentInfo);
            if (VK_RESULT != VK14.VK_SUCCESS) {
                if (VK_RESULT == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                    recreate();
                } else {
                    engine.getLogger().warn("Failed to present queue!");
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        recreate();
    }

    @Override
    public void recreate() {
        destroy();

        KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device.getPhysicalDevice().getDevice(), device.getSurface(), capabilities);

        createSwapchain(device.getLogicalDevice());
    }

    @Override
    public void destroy() {
        this.device.waitIdle();
        KHRSwapchain.vkDestroySwapchainKHR(device.getLogicalDevice().getDevice(), this.swapchain, null);
        this.imageViews.forEach(SwapchainImageView::free);
        this.images.clear();
        this.imageViews.clear();
    }

    public int getImageCount() { return this.images.size(); }

    @Override
    public void free() {
        this.destroy();
        alloc.close();
    }

    public boolean vsync() { return this.vsync; }
    public int currentImageIndex() { return this.currentImageIndex; }
    public VkExtent2D getExtent() { return this.extent; }

    public void setVsync(boolean vsync) {
        this.vsync = vsync;
        recreate();
    }


}
