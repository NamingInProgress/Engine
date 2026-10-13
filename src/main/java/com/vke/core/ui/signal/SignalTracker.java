package com.vke.core.ui.signal;

import com.carrotsearch.hppc.ObjectArrayList;

import java.util.Arrays;

public class SignalTracker {
    private final ObjectArrayList<Signal<?>.Subscription> toTrack;

    public SignalTracker() {
        this.toTrack = new ObjectArrayList<>();
    }

    private SignalTracker(Signal<?>[] signals) {
        this.toTrack = new ObjectArrayList<>(signals.length);
        for (int i = 0; i < signals.length; i++) {
            this.toTrack.buffer[i] = signals[i].subscribe();
        }
    }

    public static SignalTracker track(Signal<?>... signals) {
        return new SignalTracker(signals);
    }

    public void push(Signal<?> signal) {
        toTrack.add(signal.subscribe());
    }

    public void append(SignalTracker track) {
        int extra = track.toTrack.elementsCount;
        int left = toTrack.elementsCount;
        toTrack.ensureCapacity(left + extra);
        System.arraycopy(track.toTrack.buffer, 0, toTrack.buffer, left, extra);
    }

    public boolean changed() {
        for (int i = 0; i < toTrack.elementsCount; i++) {
            Signal<?>.Subscription sub = (Signal<?>.Subscription) toTrack.buffer[i];
            if (sub.changed()) {
                return true;
            }
        }
        return false;
    }

    public void update() {
        for (int i = 0; i < toTrack.elementsCount; i++) {
            Signal<?>.Subscription sub = (Signal<?>.Subscription) toTrack.buffer[i];
            sub.update();
        }
    }
}
