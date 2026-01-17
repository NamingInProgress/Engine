package com.vke.core.thread;

import com.vke.api.logger.Logger;
import com.vke.core.VKEngine;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TaskProcessor {
    private VKEngine engine;
    private final BlockingQueue<Task> tasks;
    private volatile boolean cancel;
    private Thread thread;

    public TaskProcessor(VKEngine engine) {
        this.engine = engine;
        this.tasks = new LinkedBlockingQueue<>();
        employ();
    }

    private void employ() {
        thread = new Thread(() -> {
            while (!cancel) {
                try {
                    Task next = tasks.poll(100, TimeUnit.MILLISECONDS);
                    if (next != null) {
                        next.work();
                    }
                } catch (InterruptedException ignore) {}
            }
        });
        thread.start();
    }

    public void free() {
        cancel = true;
        try {
            thread.join();
        } catch (InterruptedException e) {
            Logger logger = engine.getLogger();
            logger.warn("Tried to free TaskProcessor, but got interrupted", e);
        }
    }

    public void addTask(Task task) {
        tasks.add(task);
    }
}
