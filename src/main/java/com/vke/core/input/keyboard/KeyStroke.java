package com.vke.core.input.keyboard;

import java.util.HashSet;
import java.util.List;

public class KeyStroke implements InputKeyState {
    private final KeyboardInput keyboardInput;
    private final HashSet<Key> requirements;

    private final Listener.Glossary listeners = new Listener.Glossary();

    private boolean pressed;
    private boolean justPressed;
    private boolean justReleased;

    KeyStroke(KeyboardInput keyboardInput, Key... requirements) {
        this.keyboardInput = keyboardInput;
        this.requirements = new HashSet<>(List.of(requirements));
    }

    void onFrame() {
        boolean met = allRequirementsMet();

        justPressed  = met && !pressed;
        justReleased = !met && pressed;
        pressed      = met;

        if (justPressed) listeners.stateChange(State.JustPressed);
        if (justReleased) listeners.stateChange(State.JustReleased);
    }

    private boolean allRequirementsMet() {
        if (requirements.isEmpty()) return false;
        for (Key key : requirements) {
            if (!keyboardInput.key(key).isPressed()) return false;
        }
        return true;
    }

    public void addRequiredKey(Key key) {
        requirements.add(key);
    }

    public void removeRequiredKey(Key key) {
        requirements.remove(key);
    }

    @Override public boolean isPressed() {
        return pressed;
    }

    @Override public boolean wasJustPressed() {
        return justPressed;
    }

    @Override public boolean wasJustReleased() {
        return justReleased;
    }

    @Override
    public void requestState(RequestableState state) {
        //probably wont matter here
    }

    @Override
    public void listen(Listener listener) {
        listeners.addEntry(listener);
    }

    @Override
    public  void mute(Listener listener) {
        listeners.removeEntry(listener);
    }

    @Override
    public int hashCode() {
        return requirements.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof KeyStroke other)) return false;
        return requirements.equals(other.requirements);
    }
}