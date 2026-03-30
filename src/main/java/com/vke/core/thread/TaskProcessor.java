package com.vke.core.thread;

import com.vke.api.logger.Logger;
import com.vke.core.VKEngine;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TaskProcessor {
    private final VKEngine engine;
    private final BlockingQueue<Task> tasks;
    private volatile boolean cancel;
    private Thread thread;

    public TaskProcessor(VKEngine engine) {
        this.engine = engine;
        this.tasks = new LinkedBlockingQueue<>();
        employ("TaskProcessor");
    }

    public TaskProcessor(VKEngine engine, String threadName) {
        this.engine = engine;
        this.tasks = new LinkedBlockingQueue<>();
        employ(threadName);
    }

    private void employ(String name) {
        thread = new Thread(() -> {
            while (!cancel) {
                try {
                    Task next = tasks.take();
                    next.work();
                } catch (InterruptedException ignore) {}
            }
        }, name);
        thread.start();
    }

    public void free() {
        cancel = true;
        //wake up thread to allow for join to take effect
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            engine.getLogger().warn("Interrupted while stopping TaskProcessor", e);
        }
    }

    public void addTask(Task task) {
        tasks.add(task);
    }
}
