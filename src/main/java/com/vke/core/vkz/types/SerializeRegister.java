package com.vke.core.vkz.types;

import com.vke.api.serializer.Serializer;
import com.vke.core.vkz.types.imm.VkzImmediateArchive;

public class SerializeRegister {
    public static void registerVkzSerializers() {
        Serializer.registerSerializerFor(VkzImmediateArchive.class, VkzImmediateArchive.SERIALIZER);
        Serializer.registerSerializerFor(VkzDirLayer.class, VkzDirLayer.SERIALIZER);
        Serializer.registerSerializerFor(VkzName.class, VkzName.SERIALIZER);
        Serializer.registerSerializerFor(VkzEntry.class, VkzEntry.SERIALIZER);
    }
}
