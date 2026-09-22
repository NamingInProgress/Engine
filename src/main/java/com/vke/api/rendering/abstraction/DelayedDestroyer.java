package com.vke.api.rendering.abstraction;

import com.vke.api.framable.Framable;
import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.core.framable.service.FramableManager;
import com.vke.core.services2.Services;
import com.vke.utils.io.Disposable;

import java.util.LinkedList;

public class DelayedDestroyer<V extends Disposable> implements Framable, Disposable {

    private final LinkedList<Removable<V>> toRemove = new LinkedList<>();

    private final RenderSystem sys;
    private final FrameCounter fc;
    private final FramableManager fm;

    public DelayedDestroyer(RenderSystem sys) {
        this.sys = sys;
        this.fc = sys.getFrameCounter();

        this.fm = this.sys.service(Services.FRAMABLE_MANAGER);
        this.fm.registerFramable(this);
    }

    public void scheduleDestroy(V obj) {
        this.toRemove.add(new Removable<>(obj, fc.framesInFlight()));
    }

    @Override
    public void postRendering() {
        toRemove.removeIf((tr) -> {
            tr.framesLeft -= 1;
            if (tr.framesLeft == 0) {
                tr.free();
                return true;
            }
            return false;
        });
    }

    @Override
    public void free() {
        this.fm.removeFramable(this);
        this.toRemove.forEach(Disposable::free);
    }

    public static class Removable<V extends Disposable> implements Disposable {

        public int framesLeft;
        public V obj;

        public Removable(V obj, int frames) {
            this.framesLeft = frames;
            this.obj = obj;
        }

        @Override
        public void free() {
            obj.free();
        }
    }

}
