package com.vke.api.rendering.abstraction.renderer.commands;

import com.vke.api.rendering.abstraction.renderer.RenderDevice;
import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
import com.vke.api.rendering.abstraction.renderer.sync.Fence;
import com.vke.api.rendering.abstraction.renderer.sync.Semaphore;

public class ImmediateSubmit implements AutoCloseable {

    private final RenderDevice device;
    private final QueueType queueType;
    private final CommandBuffer cmd;
    private final Fence fence;
    private final Semaphore executeFinished;

    private Semaphore waitSemaphore;

    public ImmediateSubmit(RenderDevice device, QueueType qt, CommandBuffer cmd, Fence fence, Semaphore executeFinished) {
        this.device = device;
        this.queueType = qt;
        this.cmd = cmd;
        this.fence = fence;
        this.executeFinished = executeFinished;

        cmd.begin();
    }

    public Semaphore signalSemaphore() {
        return executeFinished;
    }

    public void waitSemaphore(Semaphore semaphore) {
        this.waitSemaphore = semaphore;
    }

    @Override
    public void close() {
        cmd.end();

        device.submit(cmd, new CommandBuffer.SubmitInfo(
                waitSemaphore,
                executeFinished,
                fence,
                queueType
        ));
        fence.waitForFence();
    }
}
