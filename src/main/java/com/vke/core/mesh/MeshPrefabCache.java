package com.vke.core.mesh;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Cache;
import com.vke.api.serializer.Serializer;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetCache;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.file.utils.DataUtils;
import com.vke.core.serializer.ByteLoader;
import com.vke.core.serializer.ByteSaver;
import com.vke.utils.Utils;
import com.vke.utils.io.FileUtils;
import com.vke.utils.io.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Cache
public class MeshPrefabCache implements AssetCache {
    private static final String CACHE_LOCATION = "cache/meshprefab/";
    private static final String CACHED_ASSET_EXT = ".vkmesh";

    private Path getCacheDir(Context context, Identifier assetName) throws IOException {
        String appName = context.getName();
        Path base = FileUtils.getCacheFolder(appName, true);
        return base.resolve(CACHE_LOCATION).resolve(assetName.getPath() + CACHED_ASSET_EXT);
    }

    @Override
    public String getTargetProtocol() {
        return Protocols.MESHPREFAB;
    }

    @Override
    public @Nullable AssetData checkCache(PipelineContext context, Identifier assetName) throws AssetException {
        if (Utils.TRUE) return null;

        return Utils.chainExceptions(() -> {
            Path cachedFile = getCacheDir(context, assetName);
            if (!Files.exists(cachedFile)) {
                return null;
            }
            byte[] fileData = Files.readAllBytes(cachedFile);
            ByteLoader loader = new ByteLoader(fileData);
            MeshPrefab meshPrefab = Serializer.loadObject(MeshPrefab.class, loader, false);
            return new AssetData(getTargetProtocol(), meshPrefab);
        });
    }

    @Override
    public void cacheElement(PipelineContext context, StageElement element, Identifier assetName) throws AssetException {
        Utils.chainExceptions(() -> {
            Path cachedFile = getCacheDir(context, assetName);
            MeshPrefab meshPrefab = element.getAssetData().getDataAs();
            ByteSaver saver = new ByteSaver();
            Serializer.saveObject(meshPrefab, saver, false);
            byte[] fileData = saver.asArray();
            Path parent = cachedFile.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.write(cachedFile, fileData);
            return null;
        });
    }
}
