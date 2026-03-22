package com.vke.core.event.events.assets;

import com.vke.api.assets.BundleLoadingCallback;
import com.vke.api.event.Event;

public class AssetLoadEvent extends Event {
    public final BundleLoadingCallback.AssetDesc desc;

    public AssetLoadEvent(BundleLoadingCallback.AssetDesc desc) {
        this.desc = desc;
    }
}
