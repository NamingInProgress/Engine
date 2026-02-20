package com.vke.test;

import com.vke.api.window.WindowCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.utils.Identifier;

public class AssetManagerTest {

    public static void main(String[] args) {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");

        VKEngine engine = new VKEngine(createInfo);

        new Identifier("assets").walkFiles().forEach(System.out::println);
        System.out.println("h");
        new Identifier("assets").walkDirectories(1).forEach(System.out::println);

        //AssetUtils.getBundle(engine, new Identifier("assets"));
    }

}
