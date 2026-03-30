package com.vke.api.assets;

import com.vke.core.assets.AssetException;

import java.util.Collection;

public interface BundleExchange {
    void load(String bundle);
    void loadAll(Collection<String> bundles);

    void forceUnload(String bundle);
    void forceUnload(Collection<String> bundles);

    void commit() throws AssetException;
}
