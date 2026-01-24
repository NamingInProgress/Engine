package com.vke.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static Path getConfigFolder(String appName) throws IOException {
        return getConfigFolder(appName, true);
    }

    public static Path getCacheFolder(String appName) throws IOException {
        return getCacheFolder(appName, true);
    }

    public static Path getConfigFolder(String appName, boolean makeDirs) throws IOException {
        Path p = internalGetConfigFolder(appName);
        if (makeDirs) Files.createDirectories(p);
        return p;
    }

    public static Path getCacheFolder(String appName, boolean makeDirs) throws IOException {
        Path p = internalGetCacheFolder(appName);
        if (makeDirs) Files.createDirectories(p);
        return p;
    }

    private static Path internalGetConfigFolder(String appName) {
        String home = System.getProperty("user.home");

        return switch (Utils.getOSType()) {
            case WIN -> Paths.get(System.getenv("APPDATA"), appName);
            case MAC -> Paths.get(home, "Library", "Application Support", appName);
            case LINUX -> {
                String xdg = System.getenv("XDG_CONFIG_HOME");
                if (xdg != null && !xdg.isBlank()) {
                    yield Paths.get(xdg, appName);
                }
                yield Paths.get(home, ".config", appName);
            }
        };
    }

    private static Path internalGetCacheFolder(String appName) {
        String home = System.getProperty("user.home");

        return switch (Utils.getOSType()) {
            case WIN -> Paths.get(System.getenv("LOCALAPPDATA"), appName);
            case MAC -> Paths.get(home, "Library", "Caches", appName);
            case LINUX -> {
                String xdg = System.getenv("XDG_CACHE_HOME");
                if (xdg != null && !xdg.isBlank()) {
                    yield Paths.get(xdg, appName);
                }
                yield Paths.get(home, ".cache", appName);
            }
        };
    }

}
