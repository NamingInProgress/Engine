package com.vke.core.event.events.rendering;

import com.vke.api.event.Event;

public class SwapchainEvents {

    public static class PreDestroy extends Event {}
    public static class Destroyed extends Event {}

    public static class PreCreate extends Event {}
    public static class Created extends Event {}

    public static class PreRecreate extends Event {}
    public static class Recreated extends Event {}

}
