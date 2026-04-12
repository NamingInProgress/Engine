package com.vke.core.rendering.draw;

import com.vke.api.rendering.abstraction.commands.CommandBuffer;
import com.vke.core.window.Window;

public class DrawContext {

    private final CommandBuffer cmd;
    private final Window window;

    public DrawContext(CommandBuffer cmd, Window window) {
        this.cmd = cmd;
        this.window = window;
    }

    public CommandBuffer getCommandBuffer() {
        return cmd;
    }

    public Window getWindow() {
        return window;
    }
}
