package com.vke.api.game;

import com.vke.core.VKEngine;
import com.vke.core.window.Window;

public abstract class Game {
    public abstract void onInit(VKEngine engine);
    public abstract void onDraw(Window window);
}
