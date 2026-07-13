package com.vke.api.services2;

import com.vke.utils.io.Disposable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public abstract class ServiceAPI implements Service, Lifecycle {
    private volatile ServiceImpl implementation;
    private final String id;
    private final Object lock = new Object();

    public ServiceAPI(String id, ServiceImpl baseImpl) {
        this.id = id;
        this.implementation = baseImpl;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void initialize() {
    }

    @Override
    public boolean isInitialized() {
        synchronized (lock) {
            return implementation != null && implementation.isInitialized();
        }
    }

    @SuppressWarnings("unchecked")
    public void replaceImplementation(@NotNull ServiceImpl newImpl) {
        if (implementation instanceof PinnedService) {
            String name = implementation.getId();
            String msg = String.format("Attempted to replace implementation of '%s' with %s, but '%s' is a PinnedService and cannot be altered!", name, newImpl.getClass(), name);
            newImpl.engine.throwException(new UnsupportedOperationException(msg), "ServiceAPI#replaceImplementation");
            return;
        }

        synchronized (lock) {
            Object transferState = null;

            if (implementation instanceof StatefulService<?> oldStateful) {
                transferState = oldStateful.createTransferState();
            }

            newImpl.initialize();

            if (newImpl instanceof StatefulService<?> newStateful && transferState != null) {
                ((StatefulService<Object>) newStateful).applyTransferState(transferState);
            }

            implementation = newImpl;
        }
    }

    protected void useImplementation(Consumer<ServiceImpl> action) {
        synchronized (lock) {
            if (implementation != null) {
                action.accept(implementation);
            }
        }
    }

    public ServiceImpl getImplementation() {
        synchronized (lock) {
            return implementation;
        }
    }

    @Override
    public List<String> dependencies() {
        synchronized (lock) {
            return implementation.dependencies();
        }
    }

    @Override
    public int hashCode() {
        synchronized (lock) {
            return implementation.hashCode();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceAPI api) {
            return implementation.equals(api.implementation);
        }
        return false;
    }

    @Override
    public void free() {
        useImplementation(Disposable::free);
    }
}
