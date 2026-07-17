package com.vke.core.framable.service;

import com.vke.api.framable.Framable;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.services2.Services;
import com.vke.utils.console.AnsiColors;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.vke.core.VKEngine.PROFILER;

public class FramableManagerImpl extends ServiceImpl implements FramableManager {
    private Framable.Glossary glossary;

    private final AtomicBoolean skipThisFrame = new AtomicBoolean();
    private int reqFps;
    private long targetFrameTimeNs;
    private long lastFrameTime;

    public FramableManagerImpl(VKEngine engine) {
        super(Services.FRAMABLE_MANAGER, engine);
    }

    @Override
    protected void onInitialize() {
        this.glossary = new Framable.Glossary();

        EngineCreateInfo createInfo = engine.getCreateInfo();
        this.reqFps = createInfo.fps;
        this.targetFrameTimeNs = reqFps > 0 ? 1_000_000_000L / reqFps : 0L;
        this.lastFrameTime = System.nanoTime();
    }

    @Override
    public void registerFramable(Framable framable) {
        this.glossary.addEntry(framable);
    }

    @Override
    public void removeFramable(Framable framable) {
        this.glossary.removeEntry(framable);
    }

    @Override
    public Framable.Glossary getAllFramables() {
        return glossary;
    }

    @Override
    public void handlePossibleFrame() {
        long now = System.nanoTime();

        if (reqFps != -1 && now - lastFrameTime < targetFrameTimeNs) return;

        lastFrameTime = now;

        PROFILER.beginFrame();
        PROFILER.begin("Render", AnsiColors.RED);
        PROFILER.push();
        PROFILER.begin("Frame Setup");
        skipThisFrame.setRelease(false);
        glossary.preFrame();
        PROFILER.end();
        PROFILER.pop();
        if (!skipThisFrame.get()) {
            PROFILER.begin("App Draw", AnsiColors.GREEN);
            glossary.onDraw();
            PROFILER.end();
            PROFILER.begin("Frame End");
            glossary.postFrame();
            PROFILER.end();
        }
        PROFILER.end();
        PROFILER.endFrame();
    }

    @Override
    public void skipThisFrame() {
        this.skipThisFrame.setRelease(true);
    }

    @Override
    public @Nullable FramableManager.TransferState createTransferState() {
        return new TransferState(glossary);
    }

    @Override
    public void applyTransferState(@Nullable TransferState state) {
        if (state != null) {
            this.glossary = state.registered();
        } else {
            this.glossary = new Framable.Glossary();
        }
    }

    @Override
    public List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {

    }
}
