package com.vke.core.event.events.assets;

import com.vke.api.assets.Bundle;
import com.vke.api.event.Event;

public class BundleSwapEvent extends Event {

    private final Bundle pre, post;

    public BundleSwapEvent(Bundle preSwap, Bundle postSwap) {
        this.pre = preSwap;
        this.post = postSwap;
    }

}
