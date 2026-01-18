package com.vke;

import com.vke.api.game.Game;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.logger.*;
import com.vke.core.window.Window;

public class Main {

    public static final CoreLogger LOG = LoggerFactory.get("VkEngine");

    public static void main(String[] args) {
        EngineCreateInfo createInfo = new EngineCreateInfo();
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");

        VKEngine engine = new VKEngine(createInfo);
        engine.start(new Game() {
            @Override
            public void onInit(VKEngine engine) {

            }

            @Override
            public void onDraw(Window window) {

            }
        });
    }

}
