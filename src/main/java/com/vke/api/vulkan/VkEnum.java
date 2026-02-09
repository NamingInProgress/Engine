package com.vke.api.vulkan;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public interface VkEnum {

    int getVkHandle();

    @SuppressWarnings("all")
    default <T extends VkEnum> T fromVkHandle(int vk) {
        try {
            return Arrays.stream(((T[]) getClass().getDeclaredMethod("values").invoke(this))).filter(c -> c.getVkHandle() == vk).findFirst().get();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException _) {}
        return null;
    }

}
