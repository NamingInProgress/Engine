package com.vke.core.ui.rendering.core;

import com.vke.api.app.Framable;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.vulkan.vertexconsumer.RecyclerArrayList;

public class UiCoreRenderer implements Framable {
    private static final int DEFAULT_DRAWREQ_CAP = 1024;

    private RecyclerArrayList<DrawRequest> drawRequests;

    public UiCoreRenderer() {
        this.drawRequests = new RecyclerArrayList<>(DEFAULT_DRAWREQ_CAP);
    }

    @Override
    public void preRendering(DrawContext ctx) {
        this.drawRequests.clear();
    }

    @Override
    public void onDraw(DrawContext ctx) {

    }

    @Override
    public void postRendering(DrawContext ctx) {

    }
}
