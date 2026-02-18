package com.vke.api.event;

import java.lang.invoke.MethodHandle;

public interface EventBus {

    void register(EventListener instance);
    void fire(Event event);

    record CallableHandler(EventListener instance, MethodHandle methodHandle) {}

}
