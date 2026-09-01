package com.vke.test;

import com.vke.core.color.Color;

public class ColorTest {
    public static void main(String[] args) {
        testColor("rgb(255,50%,0)");
        testColor("0xfff");
        testColor("grayscale(20%)");
        testColor("hsla(90, 25%, 0.5, 100%)");
        testColor("#00FF00AA");
        testColor("0 0 255   ");
        testColor("255  , 0  , 255");
        testColor("vke:red");
    }

    private static void testColor(String value) {
        Color color = Color.parse(null, value);
        System.out.println(color);
    }
}
