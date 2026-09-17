package com.vke.core.rendering.vulkan;

import com.vke.api.event.Event;
import com.vke.core.rendering.vulkan.device.PhysicalDevice;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

public class VKEvents {

    public static class PreInstanceCreated extends Event {
        public final VkInstanceCreateInfo createInfo;

        public PreInstanceCreated(VkInstanceCreateInfo createInfo) {
            this.createInfo = createInfo;
        }
    }

    public static class InstanceCreated extends Event {

    }

    public static class InstanceDestroyed extends Event {

    }

    public static class EvaluatePhysicalDevice extends Event {
        public final PhysicalDevice dev;
        public final int score;

        public EvaluatePhysicalDevice(PhysicalDevice dev, int score) {
            this.dev = dev;
            this.score = score;
        }
    }

    public static class ChosePhysicalDevice extends Event {
        public final PhysicalDevice dev;
        public final int score;

        public ChosePhysicalDevice(PhysicalDevice dev, int score) {
            this.dev = dev;
            this.score = score;
        }
    }

    public static class LogicalDeviceCreated extends Event {

    }

}
