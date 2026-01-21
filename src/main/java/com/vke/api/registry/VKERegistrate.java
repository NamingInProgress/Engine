package com.vke.api.registry;

import com.vke.api.serializer.Serializer;
import com.vke.utils.Identifier;

public class VKERegistrate {

    private final String addonId;

    public VKERegistrate(String addonId) {
        this.addonId = addonId;
    }

    // Add builders here
    // Example:

//    public MFLPBooleanSettingRegistrar booleanSetting(String path, boolean defaultValue) {
//        return new MFLPBooleanSettingRegistrar(id(path), defaultValue, modId);
//    }

    public Serializer<?> serializer(Class<?> clazz, Serializer<?> serializer) {
        return VKERegistries.SERIALIZERS.register(clazz, serializer);
    }

    private Identifier id(String path) { return new Identifier(addonId, path); }

}
