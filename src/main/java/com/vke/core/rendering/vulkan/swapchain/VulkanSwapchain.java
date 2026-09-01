package com.vke.core.rendering.vulkan.swapchain;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.texture.*;
import com.vke.api.rendering.abstraction.renderer.swapchain.Swapchain;
import com.vke.api.rendering.abstraction.renderer.sync.Semaphore;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.memory.intP;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
import com.vke.core.rendering.vulkan.device.VulkanQueue;
import com.vke.core.rendering.vulkan.extent.VulkanExtentUtils;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.core.rendering.vulkan.sync.VulkanSemaphore;
import com.vke.core.rendering.vulkan.texture.VulkanTexture;
import com.vke.core.rendering.vulkan.utils.VKUtils;
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

    private Format format;
    private boolean vsync;
    private long surface;

    private final ArrayList<VulkanTexture> colorImages = new ArrayList<>();

    private final VulkanRenderSystem ctx;

    private int currentImageIndex;

    private final AutoHeapAllocator alloc;

    public VulkanSwapchain(Description description, VulkanRenderSystem ctx) {
        this.ctx = ctx;
        this.vsync = description.vsync();
        this.surface = ctx.device().getSurface();
        this.alloc = new AutoHeapAllocator();

        setupInfoStructs();
        createSwapchain();
    }

    private void setupInfoStructs() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDevice device = ctx.device().getPhysicalDevice().getDevice();
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
        VkExtent2D extent2D = SwapchainUtils.chooseExtent(capabilities, alloc, ctx.windowHandle());
        int minImageCount = Math.max(3, capabilities.minImageCount());
        minImageCount = ( capabilities.maxImageCount() > 0 && minImageCount > capabilities.maxImageCount() ) ? capabilities.maxImageCount() : minImageCount;

        info.sType$Default()
                .surface(surface)
                .minImageCount(minImageCount)
                .imageFormat(pickedFormat.format())
                .imageColorSpace(pickedFormat.colorSpace())
                .imageExtent(extent2D)
                .imageArrayLayers(1)
                .imageUsage(new ImageUsage(ImageUsage.Bits.COLOR_ATTACHMENT_BIT, ImageUsage.Bits.TRANSFER_DST_BIT, ImageUsage.Bits.SAMPLED_BIT)
                        .getIntVal())
                .preTransform(capabilities.currentTransform())
                .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                .presentMode(presentMode)
                .oldSwapchain(VK14.VK_NULL_HANDLE)
                .clipped(true);

        VulkanQueue graphicsQueue = ctx.device().getQueue(QueueType.GRAPHICS);
        VulkanQueue presentQueue = ctx.device().getQueue(QueueType.PRESENT);

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

        this.format = IntEnum.fromInt(Format.values(), pickedFormat.format());
        this.extent = extent2D;

        return info;
    }

    private void createSwapchain() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pSwapChain = stack.callocLong(1);
            VkSwapchainCreateInfoKHR createInfo = getCreateInfo(stack);

            if (KHRSwapchain.vkCreateSwapchainKHR(ctx.device().vkLogicalDevice(), createInfo, null, pSwapChain) != VK14.VK_SUCCESS) {
                ctx.throwException(new IllegalStateException("Failed to create Swap Chain!"), HERE);
            }

            this.swapchain = pSwapChain.get(0);

            createImages(stack);
        }
    }

    private void createImages(MemoryStack stack) {
        LogicalDevice device = ctx.device().getLogicalDevice();
        // COLOR
        IntBuffer count = stack.mallocInt(1);
        KHRSwapchain.vkGetSwapchainImagesKHR(device.getDevice(), swapchain, count, null);

        LongBuffer images = alloc.allocLong(count.get(0)).getHeapObject();
        KHRSwapchain.vkGetSwapchainImagesKHR(device.getDevice(), swapchain, count, images);

        for (int i = 0; i < count.get(0); i++) {
            VulkanTexture image = new VulkanTexture(this.ctx, images.get(i),
                    Texture.TextureDesc.builder()
                            .size(VulkanExtentUtils.ofVk(extent))
                            .format(format)
                            .mipLevels(1)
                            .arrayLayers(1)
                            .sampleCount(SampleCount.X1)
                            .type(TextureType.TEX_2D)
                            .usage(ImageUsage.of(getCreateInfo(stack).imageUsage()))
                            .build(),
                    new ImageAspect(ImageAspect.Bits.COLOR));

            if (ctx.getEngine().isDebugMode()) {
                VKUtils.setDebugName(ctx.device().getLogicalDevice(), "swapchain" + i, image.getHandle(), VK14.VK_OBJECT_TYPE_IMAGE);
            }

            this.colorImages.add(image);
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
    public Format format() {
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
            int VK_RESULT = KHRSwapchain.vkAcquireNextImage2KHR(ctx.device().vkLogicalDevice(), acquireInfo, pNextImageIndex);
            // VK_SUCCESS and VK_SUBOPTIMAL_KHR both signal the semaphore/fence and return a valid
            // image index; only genuine error codes (negative) must skip consuming them.
            if (VK_RESULT != VK14.VK_SUCCESS && VK_RESULT != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                return ~VK_RESULT;
            }
            currentImageIndex = pNextImageIndex.get(0);
            return currentImageIndex;
        }
    }

    public VulkanTexture getColorImage(int index) { return this.colorImages.get(index); }

    public VulkanTexture getColorImage() { return this.colorImages.get(currentImageIndex()); }

    @Override
    public void present(Semaphore renderFinished) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
            presentInfo.sType$Default();
            presentInfo.pImageIndices(stack.ints(currentImageIndex));
            presentInfo.pSwapchains(stack.longs(swapchain));
            presentInfo.pWaitSemaphores(stack.longs(((VulkanSemaphore) renderFinished).getHandle()));
            presentInfo.swapchainCount(1);

            int VK_RESULT = KHRSwapchain.vkQueuePresentKHR(ctx.device().getQueue(QueueType.PRESENT).vk(), presentInfo);
            if (VK_RESULT != VK14.VK_SUCCESS) {
                if (VK_RESULT == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || VK_RESULT == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                    recreate();
                } else {
                    ctx.getLogger().warn("Failed to present queue!");
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

        KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(ctx.device().getPhysicalDevice().getDevice(), ctx.device().getSurface(), capabilities);

        createSwapchain();
    }

    @Override
    public void destroy() {
        this.ctx.device().waitIdle();
        KHRSwapchain.vkDestroySwapchainKHR(this.ctx.device().vkLogicalDevice(), this.swapchain, null);

        this.colorImages.forEach(VulkanTexture::free);

        this.colorImages.clear();
    }

    @Override
    public Texture renderTarget() {
        return getColorImage();
    }

    public int getImageCount() { return this.colorImages.size(); }

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
