package com.vke;

import com.vke.utils.serialize.ByteLoader;
import com.vke.utils.serialize.ByteSaver;
import com.vke.utils.serialize.Serializer;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Integer> ints = List.of(1, 2, 3, 4);

        ByteSaver saver = new ByteSaver();
        Serializer.saveObject(ints, saver);
        byte[] bytes = saver.asArray();

        ByteLoader loader = new ByteLoader(bytes);
        List<Integer> newInts = Serializer.loadObject(List.class, loader);
        System.out.println(newInts);
    }
}
