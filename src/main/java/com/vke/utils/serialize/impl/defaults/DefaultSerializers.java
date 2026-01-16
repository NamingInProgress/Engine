package com.vke.utils.serialize.impl.defaults;

import com.vke.utils.serialize.Serializer;

import java.util.Collection;

public class DefaultSerializers {
    private static boolean LOADED;

    public static void checkRegistration() {
        if (LOADED) return;

        Serializer.registerSerializerFor(Boolean.class, new ForBoolean());
        Serializer.registerSerializerFor(Byte.class, new ForByte());
        Serializer.registerSerializerFor(Short.class, new ForShort());
        Serializer.registerSerializerFor(Character.class, new ForCharacter());
        Serializer.registerSerializerFor(Integer.class, new ForInteger());
        Serializer.registerSerializerFor(Long.class, new ForLong());
        Serializer.registerSerializerFor(Float.class,new ForFloat());
        Serializer.registerSerializerFor(Double.class, new ForDouble());
        Serializer.registerSerializerFor(String.class, new ForString());
        Serializer.registerSerializerFor(Collection.class, new ForCollection());
        LOADED = true;
    }
}
