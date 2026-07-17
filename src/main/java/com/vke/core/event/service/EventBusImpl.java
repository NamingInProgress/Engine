package com.vke.core.event.service;

import com.vke.api.event.CancellableEvent;
import com.vke.api.event.Event;
import com.vke.api.event.EventListener;
import com.vke.api.event.SubscribeEvent;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.VKEngine;
import com.vke.core.services2.Services;
import com.vke.utils.ReflectUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventBusImpl extends ServiceImpl implements com.vke.api.event.EventBus {

    private final HashMap<Class<? extends Event>, List<CallableHandler>> handlers = new HashMap<>();

    private final VKEngine engine;

    public EventBusImpl(VKEngine engine) {
        super(Services.EVENT_BUS, engine);
        this.engine = engine;
        engine.EVENT_BUS = this;
    }

    @Override
    protected void onInitialize() {

    }

    @Override
    public void register(EventListener instance) {
        List<Method> subscribers = ReflectUtils.getAnnotatedMethods(instance.getClass(), SubscribeEvent.class);

        for (Method subscriber : subscribers) {
            Class<?>[] params = subscriber.getParameterTypes();

            if (params.length > 1) engine.throwException(
                    new RuntimeException("Found method (%s) annotated with @SubscribeEvent but with more than 1 parameter!".formatted(subscriber.getName())), "EventBus");

            try {
                if (Event.class.isAssignableFrom(params[0])) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventClass = (Class<? extends Event>) params[0];
                    handlers.computeIfAbsent(eventClass, _ -> new ArrayList<>()).add(new CallableHandler(instance, ReflectUtils.asMethodHandle(subscriber)));
                }
            } catch (IllegalAccessException iae) {
                engine.throwException(iae, "EventBus");
            }
        }
    }

    @Override
    public boolean fire(Event event) {
        handlers.getOrDefault(event.getClass(), new ArrayList<>()).forEach(handler -> {
            try {
                handler.methodHandle().invoke(handler.instance(), event);
            } catch (Throwable e) {
                engine.throwException(e, "EventBus#fire");
            }
        });

        return !(event instanceof CancellableEvent ce) || !ce.isCancelled();
    }

    @Override
    public List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {

    }
}
