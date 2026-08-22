package com.vke.test;

public class WalkFilesTest {
    public static void main(String[] args) {
        Identifier id = new Identifier("./assets", "vke", "assets");
        System.out.println("Listing files in: " + id.toActualPathString());

        for (Identifier file : id.walkFiles()) {
            System.out.println(file.toActualPathString());
        }
    }
}
