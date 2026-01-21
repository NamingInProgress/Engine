package com.vke.api.registry;

import java.util.LinkedHashMap;
import java.util.Map;

public class VKERegistries {

    private static final Map<String, VKERegistrate> REGISTRATES = new LinkedHashMap<>();

    public static VKERegistrate get(String addonId) {
        return REGISTRATES.computeIfAbsent(addonId, VKERegistrate::new);
    }

}
