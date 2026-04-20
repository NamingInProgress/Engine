package com.vke.core.vkz.service;

import com.vke.api.services2.Service;
import com.vke.api.vkz.ArchiveType;
import com.vke.api.vkz.VkzArchive;
import com.vke.api.vkz.VkzOpenException;
import com.vke.utils.io.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface Vkz extends Service {
    VkzArchive pack(Path rootDirectory) throws IOException;

    void unpackToDisk(VkzArchive archive, Path targetRoot) throws IOException;

    VkzArchive open(Identifier identifier, ArchiveType type);

    VkzArchive open(InputStream stream, ArchiveType type) throws VkzOpenException;

    VkzArchive createNew();
}
