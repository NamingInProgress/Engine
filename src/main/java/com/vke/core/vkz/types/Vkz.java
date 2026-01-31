package com.vke.core.vkz.types;

import com.vke.api.serializer.Serializer;
import com.vke.api.vkz.*;
import com.vke.core.vkz.types.imm.VkzImmediateDirLayer;
import com.vke.core.vkz.types.imm.VkzImmediateArchive;
import com.vke.utils.Utils;
import com.vke.utils.collection.IterAble;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Stack;
import java.util.stream.Stream;

public class Vkz {
    public static void registerVkzSerializers() {
        Serializer.registerSerializerFor(VkzImmediateArchive.class, VkzImmediateArchive.SERIALIZER);
        Serializer.registerSerializerFor(VkzImmediateDirLayer.class, VkzImmediateDirLayer.SERIALIZER);
        Serializer.registerSerializerFor(VkzName.class, VkzName.SERIALIZER);
        Serializer.registerSerializerFor(VkzEntry.class, VkzEntry.SERIALIZER);
    }

    public static VkzArchive pack(Path rootDirectory) throws IOException {
        VkzImmediateArchive archive = VkzImmediateArchive.empty();

        try (Stream<Path> paths = Files.list(rootDirectory)) {
            for (Path path : new IterAble<>(paths.iterator())) {
                if (Files.isDirectory(path)) {
                    packDir(archive.root(), path);
                } else {
                    packFile(archive.root(), path);
                }
            }
        }

        return archive;
    }

    private static void packDir(VkzDirectoryHandle handle, Path dir) throws IOException {
        String name = dir.getFileName().toString();
        VkzDirectoryHandle newDir = handle.createDirectory(name);

        try (Stream<Path> paths = Files.list(dir)) {
            for (Path path : new IterAble<>(paths.iterator())) {
                if (Files.isDirectory(path)) {
                    packDir(newDir, path);
                } else {
                    packFile(newDir, path);
                }
            }
        }
    }

    private static void packFile(VkzDirectoryHandle handle, Path file) throws IOException {
        String name = file.getFileName().toString();
        VkzFileHandle newFile = handle.createFile(name);
        VkzEditor editor = newFile.edit();
        byte[] bytes = Files.readAllBytes(file);
        editor.write(bytes);
        editor.commit();
    }

    public static void unpackToDisk(VkzArchive archive, Path targetRoot) throws IOException {
        VkzArchive.WalkingTree tree = archive.tree();
        Stack<Path> stack = new Stack<>();
        stack.push(targetRoot);

        for (VkzArchive.Element element : new IterAble<>(tree)) {
            while (stack.size() > element.getDepth()) {
                stack.pop();
            }

            Path currPath = stack.peek();

            if (element.isDir()) {
                Path dir = currPath.resolve(element.getName());
                Files.createDirectories(dir);
                stack.push(dir);
            } else {
                Path file = currPath.resolve(element.getName());
                try (InputStream is = element.asFile().getInputStream()) {
                    Files.write(file, is.readAllBytes());
                }
            }
        }
    }

    public static ProgressReport.Listener veryCoolListener() {
        return report -> {
            System.out.printf("[%d/%d] %s (%d bytes written)\n",
                    report.current(),
                    report.fileCount(),
                    report.currentFile(),
                    report.currentSize());
        };
    }
}
