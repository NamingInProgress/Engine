package com.vke.api.assets;

import com.vke.utils.io.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public interface BundleLoadingCallback {
    void onAssetStartLoad(AssetDesc desc);
    void onAssetEndLoad(AssetDesc desc);
    void onAssetException(AssetDesc desc, Throwable exception);
    void onLoadingComplete();

    record AssetDesc(Identifier name, int position, int totalAmount) {
        @Override
        public @NotNull String toString() {
            return String.format("[%d/%d] %s", position, totalAmount, name);
        }
    }

    class List implements BundleLoadingCallback {
        private final java.util.List<BundleLoadingCallback> callbacks;

        public List() {
            callbacks = new CopyOnWriteArrayList<>();
        }

        public void registerCallback(BundleLoadingCallback callback) {
            this.callbacks.add(callback);
        }

        public void removeCallback(BundleLoadingCallback callback) {
            this.callbacks.remove(callback);
        }

        @Override
        public void onAssetStartLoad(AssetDesc desc) {
            callbacks.forEach(c -> c.onAssetStartLoad(desc));
        }

        @Override
        public void onAssetEndLoad(AssetDesc desc) {
            callbacks.forEach(c -> c.onAssetEndLoad(desc));
        }

        @Override
        public void onAssetException(AssetDesc desc, Throwable exception) {
            callbacks.forEach(c -> c.onAssetException(desc, exception));
        }

        @Override
        public void onLoadingComplete() {
            callbacks.forEach(BundleLoadingCallback::onLoadingComplete);
        }
    }
}
