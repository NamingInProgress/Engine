package com.vke.core.vkz;

import com.vke.api.serializer.Serializer;
import com.vke.api.services.Service;
import com.vke.api.vkz.*;
import com.vke.core.services.Services;
import com.vke.core.vkz.types.VkzEntry;
import com.vke.core.vkz.types.VkzName;
import com.vke.core.vkz.types.imm.VkzImmediateDirLayer;
import com.vke.core.vkz.types.imm.VkzImmediateArchive;
import com.vke.core.vkz.types.lo.VkzListOnlyArchive;
import com.vke.utils.io.Identifier;
import com.vke.utils.collection.IterAble;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

public class Vkz extends Service {

    public Vkz() {
        super(Services.VKZ);
        registerVkzSerializers();
    }

    private static void registerVkzSerializers() {
        Serializer.registerSerializerFor(VkzImmediateArchive.class, VkzImmediateArchive.SERIALIZER);
        Serializer.registerSerializerFor(VkzImmediateDirLayer.class, VkzImmediateDirLayer.SERIALIZER);
        Serializer.registerSerializerFor(VkzName.class, VkzName.SERIALIZER);
        Serializer.registerSerializerFor(VkzEntry.class, VkzEntry.SERIALIZER);
    }
    public VkzArchive pack(Path rootDirectory) throws IOException {
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

    private void packDir(VkzDirectoryHandle handle, Path dir) throws IOException {
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

    private void packFile(VkzDirectoryHandle handle, Path file) throws IOException {
        String name = file.getFileName().toString();
        VkzFileHandle newFile = handle.createFile(name);
        VkzEditor editor = newFile.edit();
        byte[] bytes = Files.readAllBytes(file);
        editor.write(bytes);
        editor.commit();
    }

    public void unpackToDisk(VkzArchive archive, Path targetRoot) throws IOException {
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

    public VkzArchive open(Identifier identifier, ArchiveType type) {
        try {
            return open(identifier.asInputStream(), type);
        } catch (IOException e) {
            throw new VkzOpenException(e);
        }
    }

    public VkzArchive open(InputStream stream, ArchiveType type) throws VkzOpenException {
        VkzObjLoader loader = new VkzObjLoader(stream, Integer.MAX_VALUE, 0);

        VkzArchive archive = switch (type) {
            case LazyFiles -> throw new VkzOpenException("Currently no support for LazyFiles sadly :(");
            case InflateAll -> Serializer.loadObject(VkzImmediateArchive.class, loader, false);
            case ListOnly -> new VkzListOnlyArchive(loader);
            case null -> throw new VkzOpenException("Strategy " + type + " is illegal!");
        };

        try {
            stream.close();
        } catch (IOException e) {
            throw new VkzOpenException(e);
        }

        return archive;
    }

    public VkzArchive createNew() {
        return VkzImmediateArchive.empty();
    }

    @Override
    protected List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {

    }
}
