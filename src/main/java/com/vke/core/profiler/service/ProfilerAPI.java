package com.vke.core.profiler.service;

import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.services2.Services;

public class ProfilerAPI extends ServiceAPI implements Profiler {
    public ProfilerAPI(ServiceImpl baseImpl) {
        super(Services.PROFILER, baseImpl);
    }

    private Profiler getImpl() {
        return (Profiler) getImplementation();
    }

    @Override
    public void beginFrame() {
        getImpl().beginFrame();
    }

    @Override
    public void endFrame() {
        getImpl().endFrame();
    }

    @Override
    public void push() {
        getImpl().push();
    }

    @Override
    public void closeStack() {
        getImpl().closeStack();
    }

    @Override
    public void pop() {
        getImpl().pop();
    }

    @Override
    public void begin(String name) {
        getImpl().begin(name);
    }

    @Override
    public void begin(String name, String color) {
        getImpl().begin(name, color);
    }

    @Override
    public void end() {
        getImpl().end();
    }
}