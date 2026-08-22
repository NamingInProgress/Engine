package com.vke.core;

import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.core.assets.AssetException;
import com.vke.core.serializer.LoadException;
import com.vke.core.serializer.SaveException;
import com.vke.utils.Utils;
import com.vke.utils.io.FileUtils;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileIdentifier {
    private final boolean isJavaEmbed;
    private final Identifier path;
    private final String prefix;

    public FileIdentifier(boolean isJavaEmbed, Identifier path, String prefix) {
        this.isJavaEmbed = isJavaEmbed;
        this.path = path;
        this.prefix = prefix;
    }

    //example: "assets/dir:vke:my/other/path"
    public static FileIdentifier ofSafe(String value, String backupNamespace) {
        String[] parts = value.split(":", 3);
        if (parts.length == 3) {
            return new FileIdentifier(false, new Identifier(parts[1], parts[2]), parts[0]);
        } else if (parts.length == 2) {
            return new FileIdentifier(false, new Identifier(parts[0], parts[1]), "./assets");
        } else {
            return new FileIdentifier(false, new Identifier(backupNamespace, value), "./assets");
        }
    }

    public static FileIdentifier of(String value) {
        return FileIdentifier.ofSafe(value, VKEngine.VKE_NAMESPACE);
    }

    public boolean isJavaEmbed() {
        return isJavaEmbed;
    }

    public Identifier dropPrefix() {
        return path;
    }

    public String getNamespace() {
        return dropPrefix().getNamespace();
    }

    public String getPrefix() {
        return prefix;
    }

    public String toSpecialVkzFormatCuzItsBad() { return toPath().toString().replace("/", "_"); }

    public FileIdentifier extend(String extra) {
        return new FileIdentifier(isJavaEmbed, path.extend(extra), prefix);
    }

    public FileIdentifier extendRaw(String extra) {
        return new FileIdentifier(isJavaEmbed, path.extendRaw(extra), prefix);
    }

    public FileIdentifier addCachePath() throws AssetException {
        if (isJavaEmbed) {
            throw new AssetException("Cannot create cache path for embedded resource");
        }
        return new FileIdentifier(isJavaEmbed, path, prefix + "/.cache");
    }

    public FileIdentifier reconstructBundleName(String bundleKey) {
        return new FileIdentifier(isJavaEmbed, new Identifier(path.getNamespace(), bundleKey), prefix);
    }

    public Path toPath() {
        if (isJavaEmbed) {
            return Path.of(path.getCombinedPath()).normalize();
        } else {
            return Path.of(prefix + "/" + path.getCombinedPath()).normalize();
        }
    }

    public boolean existsFile() {
        if (isJavaEmbed) {
            return getClass().getClassLoader().getResource(path.getCombinedPath()) != null;
        } else {
            return Files.exists(toPath());
        }
    }

    public Iter<FileIdentifier> walkFiles() {
        return walkFiles(Integer.MAX_VALUE);
    }

    public Iter<FileIdentifier> walkFiles(int maxDepth) {
        String combined;
        if (isJavaEmbed) {
            combined = path.getCombinedPath();
        } else {
            combined = toPath().toString();
        }
        return FileUtils.getRelativePaths(isJavaEmbed, combined, maxDepth).filterMap(wf -> Option.useIf(wf.isFile(), () -> this.extend(wf.name())));
    }

    public Iter<FileIdentifier> walkDirectories() {
        return walkDirectories(Integer.MAX_VALUE);
    }

    public Iter<FileIdentifier> walkDirectories(int maxDepth) {
        String combined;
        if (isJavaEmbed) {
            combined = path.getCombinedPath();
        } else {
            combined = toPath().toString();
        }
        return FileUtils.getRelativePaths(isJavaEmbed, combined, maxDepth).filterMap(wf -> Option.useIf(!wf.isFile(), () -> this.extend(wf.name())));
    }

    public InputStream openInputStream() throws IOException {
        if (isJavaEmbed) {
            return getClass().getClassLoader().getResourceAsStream(path.getCombinedPath());
        } else {
            return Utils.chainExceptions(() -> new FileInputStream(toPath().toFile()));
        }
    }

    public OutputStream openOutputStream() throws IOException {
        if (isJavaEmbed) {
            throw new IOException("Cannot create output stream for embedded file");
        } else {
            return Utils.chainExceptions(() -> new FileOutputStream(toPath().toFile()));
        }
    }

    @Override
    public String toString() {
        return toPath().toString();
    }

    public static void registerSerializers() {
        Serializer.registerSerializerFor(FileIdentifier.class, new S());
    }

    private static class S implements Serializer<FileIdentifier> {

        @Override
        public Class<?> getObjectClass() {
            return FileIdentifier.class;
        }

        @Override
        public void save(FileIdentifier value, Saver saver) throws SaveException {
            saver.saveBoolean(value.isJavaEmbed);
            Serializer.saveObject(value.path, saver);
            if (!value.isJavaEmbed) {
                Serializer.saveObject(value.prefix, saver);
            }
        }

        @Override
        public FileIdentifier load(Loader loader) throws LoadException {
            boolean isJavaEmbed = loader.loadBoolean();
            Identifier path = Serializer.loadObject(Identifier.class, loader);
            String prefix = "";
            if (!isJavaEmbed) {
                prefix = Serializer.loadObject(String.class, loader);
            }
            return new FileIdentifier(isJavaEmbed, path, prefix);
        }
    }
}
