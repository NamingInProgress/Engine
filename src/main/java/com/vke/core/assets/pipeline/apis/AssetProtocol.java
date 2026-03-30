package com.vke.core.assets.pipeline.apis;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.Protocols;
import com.vke.core.Context;
import com.vke.core.assets.handles.utils.ProtocolAssetHandle;
import com.vke.core.assets.handles.utils.ResolvedAssetHandle;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;
import com.vke.utils.io.SegmentedPath;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public interface AssetProtocol<T> {
    String getProtocolName();
    AssetData getField(AssetData data, AssetUri uri) throws AssetException;
    Loader getLoader();
    boolean applies(AssetData a, AssetData b, Op op);

    default AssetHandle<T> createAssetHandle(AssetData data, @Nullable Loader loader) {
        if (data.isResolved()) {
            return new ResolvedAssetHandle<>(getProtocolName(), data.getDataAs());
        } else {
            return new ProtocolAssetHandle<>(Protocols.PLAIN, data.getUnresolved(), loader != null ? loader : getLoader());
        }
    }

    static AssetData getAssetData(StageElement element, String protocol) {
        if (Utils.TsContain(protocol, Protocols.FILE)) {
            return AssetData.file(element);
        }
        return element.getAssetData();
    }

    interface Loader {
        AssetData load(Context context, Identifier identifier, PipelineStage.ExecutionTarget executionTarget) throws AssetException;
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
