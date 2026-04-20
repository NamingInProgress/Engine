package com.vke.core.input.keyboard;

import com.vke.core.input.PressableState;

public interface KeyStroke extends PressableState {
    void addRequiredKey(Key key);

    void removeRequiredKey(Key key);
}
