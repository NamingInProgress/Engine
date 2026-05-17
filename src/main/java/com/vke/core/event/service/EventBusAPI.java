package com.vke.core.event.service;

import com.vke.api.event.Event;
import com.vke.api.event.EventBus;
import com.vke.api.event.EventListener;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.services2.Services;

public class EventBusAPI extends ServiceAPI implements EventBus {
    public EventBusAPI(ServiceImpl baseImpl) {
        super(Services.EVENT_BUS, baseImpl);
    }

    private EventBus getImpl() {
        return (EventBus) getImplementation();
    }

    @Override
    public void register(EventListener instance) {
        getImpl().register(instance);
    }

    @Override
    public boolean fire(Event event) {
        return getImpl().fire(event);
    }
}
