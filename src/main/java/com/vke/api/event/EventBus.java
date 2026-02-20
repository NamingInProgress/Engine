package com.vke.api.event;

import java.lang.invoke.MethodHandle;

public interface EventBus {

    void register(EventListener instance);
    /**
     * @return true when success, false when canceled
     */
    boolean fire(Event event);

    record CallableHandler(EventListener instance, MethodHandle methodHandle) {}

}
