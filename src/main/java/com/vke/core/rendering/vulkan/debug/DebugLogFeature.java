package com.vke.core.rendering.vulkan.debug;

import com.vke.api.event.SubscribeEvent;
import com.vke.api.rendering.abstraction.debug.DebugFeature;
import com.vke.api.rendering.abstraction.renderer.enums.DeviceCapabilities;
import com.vke.core.rendering.vulkan.VKEvents;
import com.vke.core.rendering.vulkan.device.DeviceUtils;
import com.vke.core.rendering.vulkan.device.PhysicalDevice;
import com.vke.core.rendering.vulkan.utils.VKUtils;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.util.Arrays;
import java.util.Iterator;

public class DebugLogFeature extends DebugFeature {

    public DebugLogFeature() {
        super(DebugFeatures.LOG_FEATURE_NAME);
    }

    @SubscribeEvent
    public void preInstanceCreation(VKEvents.PreInstanceCreated event) {
        Iterator<String> extensions = VKUtils.unwrapStrings(event.createInfo.ppEnabledExtensionNames(), event.createInfo.enabledExtensionCount());
        Iterator<String> validationLayers = VKUtils.unwrapStrings(event.createInfo.ppEnabledLayerNames(), event.createInfo.enabledLayerCount());
        logger.debug("Preparing for instance creation");
        logger.debug("Requested extensions (%d):", event.createInfo.enabledExtensionCount());
        while(extensions.hasNext()) {
            logger.debug("\t%s", extensions.next());
        }
        logger.debug("Requested validation layers (%d):", event.createInfo.enabledLayerCount());
        while (validationLayers.hasNext()) {
            logger.debug("\t%s", validationLayers.next());
        }
    }

    @SubscribeEvent
    public void postInstanceCreation(VKEvents.InstanceCreated event) {
        logger.debug("Created VK Instance");
    }

    @SubscribeEvent
    public void evaluatePhysicalDevice(VKEvents.EvaluatePhysicalDevice event) {
        PhysicalDevice d = event.dev;
        VkPhysicalDeviceProperties props = d.getProperties();
        DeviceCapabilities caps = d.getDeviceCapabilities();
        VkQueueFamilyProperties.Buffer queueFamilyProperties = d.getQueueFamilyBuffer();
        logger.debug("Evaluating physcial device: ");
        logger.debug("\tDevice: %s", d.getName());
        logger.debug("\tVendor: %s", d.getVendor());
        logger.debug("\tType: %s", caps.gpuType);
        logger.debug("\tScore: %d", event.score);
        logger.debug("\tVendor ID: %x", props.vendorID());
        logger.debug("\tDevice ID: %x", props.deviceID());
        logger.debug("\tVulkan API: %s", VKUtils.version(props.apiVersion()));
        logger.debug("\tDriver ID: %s", VKUtils.driverName(d.driverProps.driverID()));
        logger.debug("\tDriver Name: %s", d.driverProps.driverNameString());
        logger.debug("\tDriver debug: %s", d.driverProps.driverInfoString());
        logger.debug("\tDriver Conformance: %s", VKUtils.vkConformanceVersionToString(d.driverProps.conformanceVersion()));
        logger.debug("\tAvailable Queues Families (%d):", queueFamilyProperties.remaining());

        for (int i = 0; i < queueFamilyProperties.remaining(); i++) {
            VkQueueFamilyProperties q = queueFamilyProperties.get(i);
            logger.debug("\t\tQueue familiy #%d", i);
            logger.debug("\t\t\tAvailable Queue Count: %d", q.queueCount());
            logger.debug("\t\t\tAvailable Queue Types: %s", Arrays.toString(DeviceUtils.getQueueTypes(d, event.surface, i, q.queueFlags())));
            logger.debug("\t\t\tAvailable Queue Types Bits: %s", Integer.toBinaryString(q.queueFlags()));
        }
    }

    @SubscribeEvent
    public void chosePhysicalDevice(VKEvents.ChosePhysicalDevice event) {
        logger.debug("Chose GPU: " + event.dev.getName() + " with a score of " + event.score);
    }

    @Override
    public void free() {

    }

}
