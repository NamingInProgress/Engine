package com.vke.core.services2;

import com.vke.api.logger.Logger;
import com.vke.api.services2.Service;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.VKEngine;
import com.vke.core.event.events.ServiceLoadEvent;
import com.vke.utils.io.Disposable;

import java.util.*;
import java.util.stream.Collectors;

public class ServiceManager implements Disposable {
    private VKEngine engine;
    private HashMap<String, ServiceAPI> registry;
    private final Set<ServiceImpl> loadedServices = new HashSet<>();

    public ServiceManager(VKEngine engine) {
        this.engine = engine;
        this.registry = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T extends Service> T service(String key) {
        Logger logger = engine.getLogger();
        ServiceAPI api = registry.get(key);
        if (api == null) {
            logger.error("Tried to access service \"%s\", but it wasn't registered!", key);
            return null;
        }
        ServiceImpl impl = api.getImplementation();
        if (loadedServices.contains(impl)) return (T) api;

        loadedServices.add(impl);
        if (!impl.isInitialized()) {
            impl.initialize();
        }

        api.dependencies().forEach(this::service);

        engine.EVENT_BUS.fire(new ServiceLoadEvent(key, api));

        return (T) api;
    }

    public void registerNewService(String key, ServiceAPI api) {
        if (registry.containsKey(key)) {
            engine.getLogger().warn("Tried to register service %s, but it is already present! To change a services implementation, use the replaceImpl function instead!", key);
            return;
        }
        this.registry.put(key, api);
    }

    @SuppressWarnings("unchecked")
    public void replaceImpl(String key, ServiceImpl newImpl) {
        ServiceAPI api = registry.get(key);
        api.replaceImplementation(newImpl);
    }

    @Override
    public void free() {
        Set<String> names = loadedServices.stream().map(Service::getId).collect(Collectors.toSet());
        while (!loadedServices.isEmpty()) {
            List<Service> toRemove = new ArrayList<>();
            services:
            for (ServiceImpl service : loadedServices) {
                List<String> dependencies = service.dependencies();
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
