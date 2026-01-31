package com.vke;

import com.vke.api.vkz.VkzArchive;
import com.vke.core.vkz.types.Vkz;

import java.io.FileOutputStream;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Vkz.registerVkzSerializers();

        try {
            Path source = Path.of("testSourceVkz");
            VkzArchive archive = Vkz.pack(source);
            archive.tree().forEachRemaining(e -> {
                System.out.print(" ".repeat(e.getDepth()));
                System.out.println(e.getName());
            });

            System.out.println("---------------");
            archive.writeOut(new FileOutputStream("test.vkz"), Vkz.veryCoolListener());

            Vkz.unpackToDisk(archive, Path.of("testTargetVkz"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}