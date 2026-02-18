package com.vke.api.app;

import org.lwjgl.vulkan.VK14;

import java.util.Objects;

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

    public int getPatch() {
        return patch;
    }

    public int getVkFormatVersion() { return VK14.VK_MAKE_VERSION(this.major, this.minor, this.patch); }

    @Override
    public String toString() {
        return getMajor() + ":" + getMinor() + ":" + getPatch();
    }

    public static Version fromString(String s) {
        String[] vers = s.split(":");
        return new Version(Integer.parseInt(vers[0]), Integer.parseInt(vers[1]), Integer.parseInt(vers[2]));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Version version = (Version) o;
        return major == version.major && minor == version.minor && patch == version.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }
}
