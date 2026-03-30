package com.vke.utils.io;

import com.vke.utils.Utils;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

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

    public static Iter<WalkedFile> getRelativePaths(String folder, int maxDepth) {
        try {
            URL url = ClassLoader.getSystemResource(folder);
            if (url == null)
                return Iter.of();

            return switch (url.getProtocol()) {
                case "file" -> listFromFileSystem(url, maxDepth);
                case "jar"  -> listFromJar(url, maxDepth);
                default     -> Iter.of();
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Iter<WalkedFile> listFromFileSystem(URL url, int maxDepth) throws IOException, URISyntaxException {
        Path root = Paths.get(url.toURI());

        Stream<Path> stream = Files.walk(root, maxDepth);
        return Iter.of(stream)
                    .filter(p -> !p.equals(root))
                    .map(p -> {
                        String path = root.relativize(p).toString().replace('\\', '/');
                        return new WalkedFile(path, Files.isRegularFile(p));
                    })
                    .finisher(stream::close);
    }

    public static Iter<WalkedFile> listFromJar(URL url, int maxDepth) throws IOException {
        String path = url.getPath();
        String jarPath = path.substring(5, path.indexOf("!"));
        String rootEntry = path.substring(path.indexOf("!") + 2);

        if (!rootEntry.endsWith("/")) {
            rootEntry += "/";
        }

        JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8));

        Enumeration<JarEntry> entries = jar.entries();

        String finalRootEntry = rootEntry;
        return Iter.of(entries.asIterator())
                .filterMap(jarEntry -> {
                    String name = jarEntry.getName();

                    if (!name.startsWith(finalRootEntry)) {
                        return Option.none();
                    }

                    String relative = name.substring(finalRootEntry.length());

                    if (relative.isEmpty()) {
                        return Option.none();
                    }

                    int depth = relative.split("/").length;

                    if (depth > maxDepth) {
                        return Option.none();
                    }

                    return Option.some(
                            new WalkedFile(relative, !jarEntry.isDirectory())
                    );
                })
                .faultyFinisher(jar::close);
    }

    public static String getComponent(Path path, int index) {
        if (index >= 0) {
            return path.getName(index).toString();
        }
        return path.getName(path.getNameCount() + index - 1).toString();
    }

    public record WalkedFile(String name, boolean isFile) {}

    public static String getFileName(Path path) {
        return path.getFileName().toString();
    }

    public static String getExtension(Path path) {
        String filename = getFileName(path);
        int dot = filename.lastIndexOf('.');
        if (dot == -1) return null;
        return filename.substring(dot + 1);
    }

    public static String getFileNickname(Path path) {
        String filename = getFileName(path);
        int dot = filename.lastIndexOf('.');
        if (dot == -1) return null;
        return filename.substring(0, dot);
    }
}
