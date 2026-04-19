package com.vke.core.input;

public abstract class AbstractPressableState implements PressableState {
    private State currentState;
    private volatile RequestableState requestedState;
    private final Listener.Glossary listeners;

    protected AbstractPressableState() {
        this.currentState = State.Released;
        this.listeners = new Listener.Glossary();
    }

    @Override
    public boolean isPressed() {
        return currentState == State.Pressed || currentState == State.JustPressed;
    }

    @Override
    public boolean wasJustPressed() {
        return currentState == State.JustPressed;
    }

    @Override
    public boolean wasJustReleased() {
        return currentState == State.JustReleased;
    }

    @Override
    public void requestState(RequestableState state) {
        this.requestedState = state;
    }

    @Override
    public void listen(Listener listener) {
        listeners.addEntry(listener);
    }

    @Override
    public void mute(Listener listener) {
        listeners.removeEntry(listener);
    }

    public void onFrame() {
        if (currentState == State.JustPressed) {
            currentState = State.Pressed;
            listeners.stateChange(currentState);
        }

        if (requestedState == RequestableState.JustPressed) {
            currentState = State.JustPressed;
            listeners.stateChange(currentState);
        }

        if (currentState == State.JustReleased) {
            currentState = State.Released;
            listeners.stateChange(currentState);
        }

        if (requestedState == RequestableState.JustReleased) {
            currentState = State.JustReleased;
            listeners.stateChange(currentState);
        }

        requestedState = null;
    }
}
