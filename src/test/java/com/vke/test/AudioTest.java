package com.vke.test;

import javax.sound.sampled.*;

public class AudioTest {
    public static void main(String[] args) {
        System.out.println("Default mixer: " + AudioSystem.getMixer(null));

        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(info);

            System.out.println("\nMixer: " + info.getName());
            System.out.println("Description: " + info.getDescription());
            System.out.println("Vendor: " + info.getVendor());

            for (Line.Info line : mixer.getSourceLineInfo()) {
                System.out.println("  Source: " + line);
            }
        }
    }
}
