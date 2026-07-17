package com.vke.core.assets.service;

import com.vke.api.assets.BundleExchange;
import com.vke.core.assets.AssetException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BundleExchangeImpl implements BundleExchange {
    private final AssetManagerBaseImpl manager;
    final List<String> load;
    final List<String> unload;

    BundleExchangeImpl(AssetManagerBaseImpl manager) {
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
