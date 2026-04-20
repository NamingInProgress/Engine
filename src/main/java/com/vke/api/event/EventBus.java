package com.vke.api.event;

import com.vke.api.services2.Service;

import java.lang.invoke.MethodHandle;

public interface EventBus extends Service {

    void register(EventListener instance);
    /**
     * @return true when success, false when canceled
     */
    boolean fire(Event event);

    record CallableHandler(EventListener instance, MethodHandle methodHandle) {}

}
