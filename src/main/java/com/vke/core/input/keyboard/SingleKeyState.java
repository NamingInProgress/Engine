package com.vke.core.input.keyboard;

import com.vke.core.input.AbstractPressableState;

public class SingleKeyState extends AbstractPressableState {
    private final Key key;

    public SingleKeyState(Key key) {
        super();
        this.key = key;
    }
}
