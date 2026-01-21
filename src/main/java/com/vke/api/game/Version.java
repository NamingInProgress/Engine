package com.vke.api.game;

import org.lwjgl.vulkan.VK14;

public class Version {
    public static final Version V1_0_0 = new Version(1, 0, 0);

    private final int major;
    private final int minor;
    private final int patch;


    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPath() {
        return patch;
    }

    public int getVkFormatVersion() { return VK14.VK_MAKE_VERSION(this.major, this.minor, this.patch); }

}
