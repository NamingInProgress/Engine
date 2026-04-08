package com.vke.api.assets;

import com.vke.core.Context;
import com.vke.core.event.events.assets.AssetLoadEvent;
import com.vke.utils.Utils;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.Iter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class Bundle implements Disposable {
    private final Context context;

    private final HashMap<Identifier, AssetHandle<?>> assets = new HashMap<>();

    public Bundle(Context context) {
        this.context = context;
    }

    public void extendBundle(Bundle other) {
        this.assets.putAll(other.assets);
    }

    public void addAsset(Identifier identifier, AssetHandle<?> handle) {
        assets.put(identifier, handle);
    }

    public void preloadAll(@Nullable BundleLoadingCallback callback) {
        int position = 1;
        int amount = assets.size();
        for (Map.Entry<Identifier, AssetHandle<?>> entry : assets.entrySet()) {
            AssetHandle<?> handle = entry.getValue();
            Identifier name = entry.getKey();
            BundleLoadingCallback.AssetDesc desc = new BundleLoadingCallback.AssetDesc(name, position++, amount);

            try {
                if (callback != null) {
                    callback.onAssetStartLoad(desc);
                    handle.acquire(context);
                    callback.onAssetEndLoad(desc);
                } else {
                    handle.acquire(context);
                }
                context.getEngine().EVENT_BUS.fire(new AssetLoadEvent(desc));
            } catch (IOException e) {
                if (callback != null) {
                    callback.onAssetException(desc, e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> AssetHandle<T> getAsset(Identifier id) {
        return (AssetHandle<T>) assets.get(id);
    }

    @SuppressWarnings("unchecked")
    public <T> AssetHandle<T> getAsset(String name) {
        return (AssetHandle<T>) assets.get(context.id(name));
    }

    @Override
    public void free() {
        assets.values().forEach(Disposable::free);
    }

    public Iter<AssetHandle<?>> allAssets() {
        return Iter.of(assets.values());
    }
}
