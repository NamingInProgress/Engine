package com.vke.core.assets.manager;

import com.vke.api.assets.BundleExchange;
import com.vke.core.assets.AssetException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VKEBundleExchange implements BundleExchange {
    private final VKEAssetManagerService manager;
    final List<String> load;
    final List<String> unload;

    VKEBundleExchange(VKEAssetManagerService manager) {
        this.manager = manager;
        this.load = new ArrayList<>();
        this.unload = new ArrayList<>();
    }

    @Override
    public void load(String bundle) {
        load.add(bundle);
    }

    @Override
    public void loadAll(Collection<String> bundles) {
        load.addAll(bundles);
    }

    @Override
    public void forceUnload(String bundle) {
        unload.add(bundle);
    }

    @Override
    public void forceUnload(Collection<String> bundles) {
        unload.addAll(bundles);
    }

    @Override
    public void commit() throws AssetException {
        manager.commitExchange(this);
    }
}
