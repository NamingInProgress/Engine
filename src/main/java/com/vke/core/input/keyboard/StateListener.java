package com.vke.core.input.keyboard;

public class StateListener implements InputKeyState.Listener {
    private final InputKeyState.State target;
    private final Runnable runnable;

    public StateListener(InputKeyState.State target, Runnable runnable) {
        this.target = target;
        this.runnable = runnable;
    }

    @Override
    public void stateChange(InputKeyState.State newState) {
        if (newState == target) runnable.run();
    }
}
