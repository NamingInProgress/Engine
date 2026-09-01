package com.vke.core.assets;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetMeta;
import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;
import com.vke.core.assets.handles.LoadFromCacheAssetHandle;
import com.vke.core.assets.meta.AssetMetaAttributes;
import com.vke.core.assets.meta.FullAssetMeta;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.serializer.LoadException;
import com.vke.core.serializer.SaveException;
import com.vke.core.serializer.impl.load.BinaryLoader;
import com.vke.utils.Utils;
import com.vke.utils.io.FileUtils;
import com.vke.utils.iter.helpers.Option;

import java.nio.file.Files;
import java.util.Arrays;

public class CacheHandler {
    public static void registerSerializers() {
        Serializer.registerSerializerFor(CacheBlob.class, new S());
    }

    public static Option<CachedAsset> handleCachedFile(PipelineContext context, FileIdentifier file, String bundle) throws AssetException {
        return Utils.chainExceptions(() -> {
            Loader loader = new BinaryLoader(file.openInputStream());
            CacheBlob blob = Serializer.loadObject(CacheBlob.class, loader);

            if (context.getEngine().isDebugMode()) {
                if (!blob.original.existsFile()) {
                    Files.delete(file.toPath());
                    return Option.none();
                }

                byte[] hash = FileUtils.hash(blob.original);

                if (!Arrays.equals(hash, blob.hash)) {
                    Files.delete(file.toPath());
                    return Option.none();
                }
            }

            AssetProtocol<?> protocol = context.getProtocol(blob.protocol);
            AssetMeta meta = new FullAssetMeta(blob.protocol, bundle, blob.assetName, new AssetMetaAttributes());
            AssetHandle<?> handle = new LoadFromCacheAssetHandle<>(file, blob, protocol, meta);

            return Option.some(new CachedAsset(handle, blob.original));
        });
    }

    public record CachedAsset(AssetHandle<?> handle, FileIdentifier originalName) { }

    public static class CacheBlob {
        FileIdentifier original;
        Identifier assetName;
        byte[] hash;
        String protocol;

        public CacheBlob(FileIdentifier original, Identifier assetName, byte[] hash, String protocol) {
            this.original = original;
            this.assetName = assetName;
            this.hash = hash;
            this.protocol = protocol;
        }
    }

    private static class S implements Serializer<CacheBlob> {

        @Override
        public Class<?> getObjectClass() {
            return CacheBlob.class;
        }

        @Override
        public void save(CacheBlob value, Saver saver) throws SaveException {
            Serializer.saveObject(value.original, saver, false);
            Serializer.saveObject(value.assetName, saver, false);
            saver.saveByteArray(value.hash);
            Serializer.saveObject(value.protocol, saver, false);
        }

        @Override
        public CacheBlob load(Loader loader) throws LoadException {
            FileIdentifier original = Serializer.loadObject(FileIdentifier.class, loader);
            Identifier assetName = Serializer.loadObject(Identifier.class, loader);
            byte[] hash = loader.loadByteArray();
            String protocol = Serializer.loadObject(String.class, loader);
            return new CacheBlob(original, assetName, hash, protocol);
        }
    }

}
