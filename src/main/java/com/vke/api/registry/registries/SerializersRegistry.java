package com.vke.api.registry.registries;

import com.vke.api.serializer.Serializer;
import com.vke.utils.Identifier;

public class SerializersRegistry extends VKERegistry<Class<?>, Serializer<?>> {

    public SerializersRegistry(Identifier registryName) {
        super(registryName);
    }

}
