package com.vke.core.rendering.vulkan.debug;

import com.vke.api.event.SubscribeEvent;
import com.vke.api.rendering.abstraction.debug.DebugFeature;
import com.vke.api.rendering.abstraction.renderer.enums.DeviceCapabilities;
import com.vke.core.rendering.vulkan.VKEvents;
import com.vke.core.rendering.vulkan.device.PhysicalDevice;
import com.vke.core.rendering.vulkan.utils.VKUtils;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

public class DebugLogFeature extends DebugFeature {

    public static final String NAME = "Logging Feature";

    public DebugLogFeature() {
        super(DebugLogFeature.NAME);
    }

    @SubscribeEvent
    public void preInstanceCreation(VKEvents.PreInstanceCreated event) {
        logger.debug("Preparing for instance creation");
        // Print expected exts and shit
    }

    @SubscribeEvent
    public void postInstanceCreation(VKEvents.InstanceCreated event) {
        logger.debug("Created VK Instance");
        logger.debug("Properties: ");
    }

    @SubscribeEvent
    public void evaluatePhysicalDevice(VKEvents.EvaluatePhysicalDevice event) {
        PhysicalDevice d = event.dev;
        VkPhysicalDeviceProperties props = d.getProperties();
        DeviceCapabilities caps = d.getDeviceCapabilities();
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
    }

    @SubscribeEvent
    public void chosePhysicalDevice(VKEvents.ChosePhysicalDevice event) {
        logger.debug("Chose GPU: " + event.dev.getName() + " with a score of " + event.score);
    }

    @Override
    public void free() {

    }

}
