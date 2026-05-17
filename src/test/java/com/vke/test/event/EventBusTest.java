package com.vke.test.event;

import com.vke.api.event.EventListener;
import com.vke.api.event.SubscribeEvent;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.services2.Services;

public class EventBusTest implements EventListener {

    public static void main(String[] args) {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");

        VKEngine engine = new VKEngine(createInfo);
        engine.service(Services.EVENT_BUS);

        engine.EVENT_BUS.register(new EventBusTest());
        engine.EVENT_BUS.fire(new Event67());
    }

    @SubscribeEvent
    public void on67Event(Event67 event) {
        event.get67();
    }

}
