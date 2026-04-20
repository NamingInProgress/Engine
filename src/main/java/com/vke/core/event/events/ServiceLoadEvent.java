package com.vke.core.event.events;

import com.vke.api.event.Event;
import com.vke.api.services2.Service;

public class ServiceLoadEvent extends Event {

    private final String name;
    private final Service service;

    public ServiceLoadEvent(String name, Service service) {
        this.name = name;
        this.service = service;
    }

    public String getName() {
        return name;
    }

    public Service getService() {
        return service;
    }
}
