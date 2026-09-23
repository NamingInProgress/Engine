package com.vke.core.rendering.vulkan.device;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.api.rendering.abstraction.renderer.enums.DeviceCapabilities;
import com.vke.api.rendering.abstraction.renderer.enums.GpuType;
import com.vke.core.rendering.vulkan.createInfos.VulkanCreateInfo;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.stream.Collectors;

public class PhysicalDevice implements Disposable {

    public final VkPhysicalDevice vk;
    public final VkPhysicalDeviceProperties props;
    public final VkPhysicalDeviceProperties2 props2;
    public final VkPhysicalDeviceDriverProperties driverProps;
    public final VkExtensionProperties.Buffer extensionBuffer;
    public final VkQueueFamilyProperties.Buffer queueFamilyBuffer;
    public final Vendor vendor;

    private DeviceCapabilities caps;

    private final AutoHeapAllocator alloc;

    public PhysicalDevice(VkPhysicalDevice vk) {
        this.vk = vk;
        this.alloc = new AutoHeapAllocator();

        props = alloc.allocStruct(VkPhysicalDeviceProperties.SIZEOF, VkPhysicalDeviceProperties::new);
        VK14.vkGetPhysicalDeviceProperties(vk, props);

        driverProps = alloc.allocStruct(VkPhysicalDeviceDriverProperties.SIZEOF, VkPhysicalDeviceDriverProperties::new);
        driverProps.sType$Default();

        props2 = alloc.allocStruct(VkPhysicalDeviceProperties2.SIZEOF, VkPhysicalDeviceProperties2::new);
        props2.sType$Default();
        props2.pNext(driverProps.address());
        VK14.vkGetPhysicalDeviceProperties2(vk, props2);

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

        this.vendor = Vendor.fromBits(props.vendorID());
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

    public Vendor getVendor() { return this.vendor; }

    public List<VkQueueFamilyProperties> getRequiredProperties(VulkanCreateInfo vkCreateInfo) {
        int bits = vkCreateInfo.requiredQueueFamilyBits;
        return this.getQueueFamilyBuffer().stream().filter((props) ->
                (bits & props.queueFlags()) == bits).collect(Collectors.toList());
    }

    public VKCapabilitiesInstance getCapabilities() {
        return this.vk.getCapabilities();
    }

    public DeviceCapabilities getDeviceCapabilities() {
        if (this.caps != null) return caps;

        this.caps = new DeviceCapabilities();
        VkPhysicalDeviceProperties props = getProperties();
        VkPhysicalDeviceLimits limits = props.limits();

        caps.maxTexture2DSize = limits.maxImageDimension2D();
        caps.maxTexture3DSize = limits.maxImageDimension3D();
        caps.maxCubeMapSize = limits.maxImageDimensionCube();

        caps.maxUBOSize = limits.maxUniformBufferRange();
        caps.maxSSBOSize = limits.maxStorageBufferRange();

        caps.maxColorAttachments = limits.maxColorAttachments();
        caps.maxDescriptorSets = limits.maxBoundDescriptorSets();

        caps.maxVertexAttributes = limits.maxVertexInputAttributes();

        caps.maxPushConstantSize = limits.maxPushConstantsSize();

        caps.gpuType = IntEnum.fromInt(GpuType.values(), props.deviceType());

        caps.minUboAlign = limits.minUniformBufferOffsetAlignment();
        caps.minSSBOAlign = limits.minStorageBufferOffsetAlignment();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceDescriptorIndexingProperties indexingProps = VkPhysicalDeviceDescriptorIndexingProperties.calloc(stack)
                    .sType$Default();
            VkPhysicalDeviceProperties2 props2 = VkPhysicalDeviceProperties2.calloc(stack)
                    .sType$Default()
                    .pNext(indexingProps);

            VK14.vkGetPhysicalDeviceProperties2(vk, props2);

            caps.maxBindlessSampledImages = indexingProps.maxDescriptorSetUpdateAfterBindSampledImages();

            VkFormatProperties vkProps = VkFormatProperties.malloc(stack);

            VK14.vkGetPhysicalDeviceFormatProperties(
                    getDevice(),
                    VK14.VK_FORMAT_S8_UINT,
                    vkProps
            );

            boolean supported =
                    (vkProps.optimalTilingFeatures()
                            & VK14.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT) != 0;

            if (!supported) {
                throw new RuntimeException("Stencil-only format not supported!");
            }
        }

        return caps;
    }

    @Override
    public void free() {
        alloc.close();
    }

    public enum Vendor {
        NVIDIA(0x10DE),
        AMD(0x1002),
        Intel(0x8086),
        ARM(0x13B5),
        Qualcomm(0x5143),
        Apple(0x106B),
        Unknown(0x0000);

        public final int vendorID;

        Vendor(int vendorID) {
            this.vendorID = vendorID;
        }

        public static Vendor fromBits(int vendorID) {
            for (Vendor vendor : Vendor.values()) {
                if (vendor.vendorID == vendorID) return vendor;
            }
            return Vendor.Unknown;
        }
    }

}
