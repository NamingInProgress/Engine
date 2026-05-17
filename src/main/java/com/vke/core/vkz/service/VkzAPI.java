package com.vke.core.vkz.service;

import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.api.vkz.ArchiveType;
import com.vke.api.vkz.VkzArchive;
import com.vke.api.vkz.VkzOpenException;
import com.vke.core.services2.Services;
import com.vke.utils.io.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class VkzAPI extends ServiceAPI implements Vkz {
    public VkzAPI(ServiceImpl baseImpl) {
        super(Services.VKZ, baseImpl);
    }

    private Vkz getImpl() {
        return (Vkz) getImplementation();
    }

    @Override
    public VkzArchive pack(Path rootDirectory) throws IOException {
        return getImpl().pack(rootDirectory);
    }

    @Override
    public void unpackToDisk(VkzArchive archive, Path targetRoot) throws IOException {
        getImpl().unpackToDisk(archive, targetRoot);
    }

    @Override
    public VkzArchive open(Identifier identifier, ArchiveType type) {
        return getImpl().open(identifier, type);
    }

    @Override
    public VkzArchive open(InputStream stream, ArchiveType type) throws VkzOpenException {
        return getImpl().open(stream, type);
    }

    @Override
    public VkzArchive createNew() {
        return getImpl().createNew();
    }
}
