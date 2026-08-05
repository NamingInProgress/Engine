package com.vke.test.ttf;

import com.vke.core.font.ttf.TTFFont;

import java.io.*;

public class TTFReadTest {

    public static void main(String[] args) throws Exception {
        try (InputStream stream = new FileInputStream("JetBrainsMono-Bold.ttf")) {
            TTFFont file = new TTFFont(stream);
        }
    }

}
