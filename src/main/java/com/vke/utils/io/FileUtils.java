package com.vke.utils.io;

import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;
import com.vke.utils.Utils;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class FileUtils {

    public static final String HASH_ALGORITHM = "SHA-256";

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

    public static Iter<WalkedFile> getRelativePaths(boolean isJavaEmbedded, String folder, int maxDepth) {
        if (isJavaEmbedded) {
            try {
                Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(folder);

                if (!urls.hasMoreElements()) {
                    return Iter.of();
                }

                List<Iter<WalkedFile>> iters = new ArrayList<>();

                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    iters.add(walkUrlResources(url, maxDepth));
                }

                return Iter.of(iters).flatMap(it -> it);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                var top = Paths.get(folder);
                var stream = Files.walk(top, maxDepth);
                return Iter.of(stream)
                        .filter(f -> !f.equals(top))
                        .map(f -> {
                            boolean isDir = Files.isDirectory(f);
                            Path between = top.relativize(f);
                            String name = between.normalize().toString();
                            return new WalkedFile(name, !isDir);
                        }).finisher(stream::close);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Iter<WalkedFile> walkUrlResources(URL url, int maxDepth) throws URISyntaxException, IOException {
        URI uri = url.toURI();
        if ("jar".equals(uri.getScheme())) {
            FileSystem fileSystem;
            try {
                fileSystem = FileSystems.getFileSystem(uri);
            } catch (FileSystemNotFoundException e) {
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            }

            String ssp = uri.getRawSchemeSpecificPart();
            int separatorIdx = ssp.indexOf("!");
            String internalPath = separatorIdx != -1 ? ssp.substring(separatorIdx + 1) : "/";

            Path root = fileSystem.getPath(internalPath);
            return processStream(root, maxDepth, fileSystem);
        }

        Path root = Paths.get(uri);
        return processStream(root, maxDepth, null);
    }

    private static Iter<WalkedFile> processStream(Path root, int maxDepth, FileSystem closeableFs) throws IOException {
        Stream<Path> stream = Files.walk(root, maxDepth);

        return Iter.of(stream)
                .filter(p -> !p.equals(root))
                .map(p -> {
                    String relativePath = root.relativize(p).toString().replace('\\', '/');
                    return new WalkedFile(relativePath, Files.isRegularFile(p));
                })
                .finisher(() -> {
                    stream.close();
                    if (closeableFs != null) {
                        try {
                            closeableFs.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    public static String getComponent(Path path, int index) {
        if (index >= 0) {
            return path.getName(index).toString();
        }
        return path.getName(path.getNameCount() + index - 1).toString();
    }

    public static String getSubpath(Path path, int startIdx) {
        return FileUtils.toNormalPath(path.subpath(startIdx, path.getNameCount()).toString());
    }

    public static String toNormalPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    public static String toNormalPath(String path) {
        return path.replace('\\', '/');
    }

    public static String getFilename(FileIdentifier ident) {
        Identifier filename = ident.dropPrefix().strip();
        return filename.getPath();
    }

    public static String getFilenameLower(FileIdentifier ident) {
        return getFilename(ident).toLowerCase(Locale.ROOT);
    }

    public static String getExtension(FileIdentifier ident) {
        String filename = getFilename(ident);
        int dotIdx = filename.lastIndexOf('.');
        if (dotIdx < 0) return filename;
        return filename.substring(dotIdx + 1);
    }

    public static String getExtensionLower(FileIdentifier ident) {
        return getExtension(ident).toLowerCase(Locale.ROOT);
    }

    public static String getFileNickname(FileIdentifier ident) {
        String filename = getFilename(ident);
        int dotIdx = filename.lastIndexOf('.');
        if (dotIdx < 0) return filename;
        return filename.substring(0, dotIdx);
    }

    public static String getFileNicknameLower(FileIdentifier ident) {
        return getFileNickname(ident).toLowerCase(Locale.ROOT);
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

    public static byte[] hash(FileIdentifier file) throws IOException {
        return hash(file, HASH_ALGORITHM);
    }

    public static byte[] hash(FileIdentifier file, String hashAlgo) throws IOException {
        return Utils.chainExceptions(() -> {
            MessageDigest digestionTrack = MessageDigest.getInstance(hashAlgo);
            try (InputStream is = Files.newInputStream(file.toPath())) {
                DigestInputStream dis = new DigestInputStream(is, digestionTrack);
                //we can actually avoid a lot of memory usage by reading in chunks lol
                final int CHUNKS = 1024;
                int read = 1;
                while (read > 0) {
                    read = dis.readNBytes(CHUNKS).length;
                }
            }
            return digestionTrack.digest();
        });
    }
}
