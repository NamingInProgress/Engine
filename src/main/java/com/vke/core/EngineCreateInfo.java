package com.vke.core;

import com.vke.api.game.Version;
import com.vke.api.window.WindowCreateInfo;

import java.util.ArrayList;
import java.util.List;

public class EngineCreateInfo {

    public WindowCreateInfo windowCreateInfo;
    public String applicationName;
    public Version applicationVersion;
    public boolean releaseMode;

    public final String engine = "VkEngine";
    public final Version engineVersion = Version.V1_0_0;
    public final Version vkAPIVersion = new Version(1, 4, 0);
    public final List<String> vkExtensions = new ArrayList<>();

    public EngineCreateInfo() {
        windowCreateInfo = new WindowCreateInfo();
        applicationName = "HelloApplication";
        applicationVersion = Version.V1_0_0;
        releaseMode = true;
    }
}
