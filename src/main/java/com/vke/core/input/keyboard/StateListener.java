package com.vke.core.input.keyboard;

import com.vke.core.input.PressableState;

public class StateListener implements PressableState.Listener {
    private final PressableState.State target;
    private final Runnable runnable;

    public StateListener(PressableState.State target, Runnable runnable) {
        this.target = target;
        this.runnable = runnable;
    }

    @Override
    public void stateChange(PressableState.State newState) {
        if (newState == target) runnable.run();
    }
}
