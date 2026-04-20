package com.vke.core.input.keyboard;

import com.vke.core.input.PressableState;

public interface KeyboardInput extends KeyListener {
    PressableState key(Key key);

    void registerListener(KeyListener listener);

    void removeListener(KeyListener listener);

    KeyStroke keyStroke(Key... keys);
}
