package com.vke.api.event;

public interface EventListener {
    default int priority() { return 0; }
}
