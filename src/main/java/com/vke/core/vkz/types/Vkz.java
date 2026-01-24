package com.vke.core.vkz.types;

import com.vke.api.serializer.Serializer;
import com.vke.core.vkz.types.imm.VkzImmediateDirLayer;
import com.vke.core.vkz.types.imm.VkzImmediateArchive;

public class Vkz {
    public static void registerVkzSerializers() {
        Serializer.registerSerializerFor(VkzImmediateArchive.class, VkzImmediateArchive.SERIALIZER);
        Serializer.registerSerializerFor(VkzImmediateDirLayer.class, VkzImmediateDirLayer.SERIALIZER);
        Serializer.registerSerializerFor(VkzName.class, VkzName.SERIALIZER);
        Serializer.registerSerializerFor(VkzEntry.class, VkzEntry.SERIALIZER);
    }
}
