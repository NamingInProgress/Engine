package com.vke.core.input.mouse;

import com.vke.api.app.Framable;
import com.vke.core.input.PressableState;

public interface MouseInput extends Framable, ButtonListener {
    MousePositionState position();

    MouseScrollState scroll();

    PressableState button(Button button);
}
