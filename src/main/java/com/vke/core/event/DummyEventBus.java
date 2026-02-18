package com.vke.core.event;

import com.vke.api.event.Event;
import com.vke.api.event.EventBus;
import com.vke.api.event.EventListener;

public class DummyEventBus implements EventBus {

    @Override
    public void register(EventListener instance) {}

    @Override
    public void fire(Event event) {}

}
