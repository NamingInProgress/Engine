package com.vke.core.event.service;

import com.vke.api.event.Event;
import com.vke.api.event.IEventBus;
import com.vke.api.event.EventListener;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.services2.Services;

public class EventBusAPI extends ServiceAPI implements IEventBus {
    public EventBusAPI(ServiceImpl baseImpl) {
        super(Services.EVENT_BUS, baseImpl);
    }

    private IEventBus getImpl() {
        return (IEventBus) getImplementation();
    }

    @Override
    public void register(EventListener instance) {
        getImpl().register(instance);
    }

    @Override
    public void remove(EventListener instance) {
        getImpl().remove(instance);
    }

    @Override
    public boolean fire(Event event) {
        return getImpl().fire(event);
    }
}
