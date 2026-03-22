package com.vke.core.services;

import com.vke.api.logger.Logger;
import com.vke.api.registry.VKERegistries;
import com.vke.api.services.Service;
import com.vke.api.services.ServiceCreateContext;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.event.events.ServiceLoadEvent;
import com.vke.core.logger.LoggerFactory;
import com.vke.utils.io.Disposable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceManager implements Disposable {

    private static final Logger logger = LoggerFactory.get("Service Manager");

    private final Set<Service> loadedServices = new HashSet<>();

    private final ServiceCreateContext scc;
    private final VKEngine engine;

    public ServiceManager(ServiceCreateContext scc) {
        this.scc = scc;
        this.engine = scc.engine();

        Services.init();
    }

    @SuppressWarnings("unchecked")
    public <T> T service(String key) {
        Service s = VKERegistries.SERVICES.get(key, scc);
        if (s == null) {
            logger.error("Tried to access service \"%s\", but it wasn't registered!", key);
            return null;
        }

        if (loadedServices.contains(s)) return (T) s;

        loadedServices.add(s);
        s.getDependencies().forEach(this::service);

        engine.EVENT_BUS.fire(new ServiceLoadEvent(key, s));

        return (T) s;
    }

    public boolean isServiceLoaded(String key) {
        return loadedServices.stream().anyMatch(service -> service.getId().equals(key));
    }

    @Override
    public void free() {
        Set<String> names = loadedServices.stream().map(Service::getId).collect(Collectors.toSet());
        while (!loadedServices.isEmpty()) {
            List<Service> toRemove = new ArrayList<>();
            services:
            for (Service service : loadedServices) {
                List<String> dependencies = service.getDependencies();
                for (String dep : dependencies) {
                    if (names.contains(dep)) {
                        continue services;
                    }
                }
                service.freeSafe();
                toRemove.add(service);
            }

            if (toRemove.isEmpty()) {
                throw new IllegalStateException("Circular dependency detected between services: " + names);
            }

            for (Service s : toRemove) {
                names.remove(s.getId());
                loadedServices.remove(s);
            }
        }
    }
}
