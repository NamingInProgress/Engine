package com.vke.core.input.keyboard;

import com.vke.utils.collection.AbstractGlossary;

public interface InputKeyState {
    boolean isPressed();
    boolean wasJustPressed();
    boolean wasJustReleased();

    void requestState(RequestableState state);

    void listen(Listener listener);
    void mute(Listener listener);

    enum RequestableState {
        JustPressed,
        JustReleased
    }

    enum State {
        JustPressed,
        Pressed,
        JustReleased,
        Released
    }

    interface Listener {
        void stateChange(State newState);

        class Glossary extends AbstractGlossary<Listener> implements Listener {
            @Override
            public void stateChange(State newState) {
                entries.forEach(listener -> listener.stateChange(newState));
            }
        }
    }
}
