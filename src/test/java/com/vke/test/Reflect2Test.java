package com.vke.test;

import com.vke.core.rendering.reflection2.CoreReflector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Reflect2Test {
    public static void main(String[] args) throws FileNotFoundException {
        CoreReflector reflector = new CoreReflector(new FileInputStream("src/test/resources/cube.vert.spv"));
        reflector.reflect(null, null, null);
    }
}