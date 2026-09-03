package com.vke.core.assets.pipeline.apis;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetMeta;
import com.vke.api.assets.Protocols;
import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;
import com.vke.core.assets.handles.ProtocolAssetHandle;
import com.vke.core.assets.handles.ResolvedAssetHandle;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.meta.FullAssetMeta;
import com.vke.core.assets.meta.AssetMetaAttributes;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.core.serializer.LoadException;
import com.vke.core.serializer.SaveException;
import com.vke.utils.Utils;
import com.vke.utils.io.SegmentedPath;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public interface AssetProtocol<T> {
    String getProtocolName();
    AssetData getField(AssetData data, AssetUri uri) throws AssetException;
    Loader getLoader();
    boolean applies(AssetData a, AssetData b, Op op);

    default boolean isCacheable() {
        return false;
    }

    default AssetHandle<T> createAssetHandle(StageElement element, Identifier assetName, @Nullable Loader loader) {
        AssetData data = element.getAssetData();
        AssetMetaAttributes vkeMeta = element.getMetaAttributes();
        if (data.isResolved()) {
            AssetMeta meta = new FullAssetMeta(getProtocolName(), element.getBundleName(), assetName, vkeMeta);
            return new ResolvedAssetHandle<>(data.getDataAs(), meta);
        } else {
            AssetMeta meta = new FullAssetMeta(Protocols.PLAIN, element.getBundleName(), assetName, vkeMeta);
            return new ProtocolAssetHandle<>(data.getUnresolved(), loader != null ? loader : getLoader(), meta);
        }
    }

    static AssetData getAssetData(StageElement element, String protocol) {
        if (Utils.TsContain(protocol, Protocols.FILE)) {
            return AssetData.file(element);
        }
        return element.getAssetData();
    }

    default void serializeData(T data, com.vke.api.serializer.Saver saver) throws SaveException {
        throw new SaveException("Cannot serialize '" + getProtocolName() + "' assets!");
    }

    default T deserializeData(com.vke.api.serializer.Loader loader) throws LoadException {
        throw new LoadException("Cannot deserialize '" + getProtocolName() + "' assets!");
    }

    interface Loader {
        AssetData load(Context context, FileIdentifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException;
    }

    class Router {
        private final HashMap<SegmentedPath, DataGetter> paths;
        private final String protocolName;

        public Router(String protocolName) {
            this.protocolName = protocolName;
            this.paths = new HashMap<>();
        }

        public void addPath(SegmentedPath path, DataGetter getter) {
            this.paths.put(path, getter);
        }

        public AssetData getDataAt(SegmentedPath path, StageElement element) {
            DataGetter getter = this.paths.get(path);
            if (getter != null) {
                return getter.getFor(element);
            }
            return null;
        }

        public AssetData getDataAtSafe(SegmentedPath path, StageElement element) throws AssetException {
            AssetData probably = getDataAt(path, element);
            if (probably == null) {
                StringBuilder msgBuilder = new StringBuilder("Cannot apply selector ");
                msgBuilder.append(path);
                msgBuilder.append(" for protocol ");
                msgBuilder.append(protocolName);
                msgBuilder.append("! Allowed are:");
                msgBuilder.append(System.lineSeparator());
                for (SegmentedPath p : paths.keySet()) {
                    msgBuilder.append('\t').append(p).append(System.lineSeparator());
                }
                throw new AssetException(msgBuilder.toString());
            }
            return probably;
        }

        @FunctionalInterface
        public interface DataGetter {
            AssetData getFor(StageElement element);
        }
    }
}
