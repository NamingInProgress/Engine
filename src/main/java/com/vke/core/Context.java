package com.vke.core;

import com.vke.api.app.Namespace;
import com.vke.api.logger.Logger;
import com.vke.api.services2.ScopedServiceImpl;
import com.vke.api.services2.Service;
import com.vke.api.services2.ServiceAPI;
import com.vke.core.logger.LoggerFactory;
import com.vke.utils.Infallible;
import org.jetbrains.annotations.NotNull;

public abstract class Context implements Namespace {
    private final Namespace namespace;
    private final Logger logger;

    Context(Namespace namespace) {
        this.namespace = namespace;
        this.logger = LoggerFactory.get(namespace.getName());
    }

    public Logger getLogger() {
        return logger;
    }

    public @NotNull Infallible throwException(Throwable e, String where) {
        getLogger().fatal("Fatal exception at %s", where);
        throw new RuntimeException(e);
    }

    @Override
    public String getName() {
        return namespace.getName();
    }

    @Override
    public Identifier id(String value) {
        return namespace.id(value);
    }

    @Override
    public FileIdentifier fid(String value) {
        Identifier id = namespace.id(value);
        return new FileIdentifier(false, id, "./assets");
    }

    public abstract VKEngine getEngine();

    @SuppressWarnings("unchecked")
    public <T extends Service> T service(String key) {
        Service service = getEngine().getServiceManager().service(key);
        if (service instanceof ServiceAPI api) {
            if (api.getImplementation() instanceof ScopedServiceImpl<?> scopedService) {
                return (T) scopedService.getScoped(this);
            }
        }
        return (T) service;
    }
}
