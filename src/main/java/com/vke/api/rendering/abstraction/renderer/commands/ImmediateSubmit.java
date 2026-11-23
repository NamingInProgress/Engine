package com.vke.api.rendering.abstraction.renderer.commands;

import com.vke.api.rendering.abstraction.BasicRenderAllocator;
import com.vke.api.rendering.abstraction.renderer.RenderDevice;
import com.vke.api.rendering.abstraction.renderer.data.IRenderAllocator;
import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
import com.vke.api.rendering.abstraction.renderer.sync.Fence;
import com.vke.api.rendering.abstraction.renderer.sync.Semaphore;
import com.vke.utils.io.Disposable;

public class ImmediateSubmit implements AutoCloseable, Disposable {

    private final RenderDevice device;
    private final QueueType queueType;
    private final CommandBuffer cmd;
    private final Fence fence;
    private final Semaphore executeFinished;
    private final IRenderAllocator allocator;

    private Semaphore waitSemaphore;
    private boolean manualFree;

    public ImmediateSubmit(RenderDevice device, QueueType qt, CommandBuffer cmd, Fence fence, Semaphore executeFinished) {
        this.device = device;
        this.queueType = qt;
        this.cmd = cmd;
        this.fence = fence;
        this.executeFinished = executeFinished;
        this.allocator = new BasicRenderAllocator();

        if (fence != null) {
            this.fence.reset();
        }

        cmd.begin();
    }

    public Semaphore signalSemaphore() {
        return executeFinished;
    }
    public CommandBuffer cmd() { return cmd; }
    public IRenderAllocator allocator() { return allocator; }

    public void waitSemaphore(Semaphore semaphore) {
        this.waitSemaphore = semaphore;
    }

    public void manualFree(boolean manualFree) {
        this.manualFree = manualFree;
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

        if (fence != null) {
            fence.waitForFence();
        }

        if (!manualFree) {
            free();
        }
    }

    @Override
    public void free() {
        allocator.close();
        cmd.free();
        if (fence != null)
            fence.free();
        if (executeFinished != null)
            executeFinished.free();
    }
}
