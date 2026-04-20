package com.vke.core.profiler.service;

import com.vke.api.services2.Service;

public interface Profiler extends Service {
    void beginFrame();

    void endFrame();

    void push();

    void closeStack();

    void pop();

    void begin(String name);

    void begin(String name, String color);

    void end();
}
