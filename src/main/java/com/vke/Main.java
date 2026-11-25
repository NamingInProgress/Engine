package com.vke;

import com.vke.api.event.EventBus;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.window.WindowCreateInfo;
import com.vke.config.ConfigurationOption;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.event.events.ServiceLoadEvent;
import com.vke.core.logger.CoreLogger;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.rendering.vulkan.debug.DebugLogFeature;
import com.vke.core.rendering.vulkan.debug.DebugMemoryTrackingFeature;
import com.vke.core.scene.SceneApp;
import com.vke.core.services2.Services;
import com.vke.utils.Infallible;
import com.vke.utils.Utils;
import pl.epsi.jenderdoc.CaptureOption;
import pl.epsi.jenderdoc.JenderDoc;
import pl.epsi.jenderdoc.RenderDoc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Main {

    public static final CoreLogger LOG = LoggerFactory.get("VkEngine");

    public static void main(String[] args) throws InterruptedException, ConfigParser.ConfigParseException, IOException, SchemaMismatchException {
//        System.los ad("/usr/lib/librenderdoc.so");
        JenderDoc.init();
        RenderDoc.captureOption(CaptureOption.RefAllResources, 1);
        RenderDoc.captureOption(CaptureOption.CaptureAllCmdLists, 1);
        RenderDoc.captureOption(CaptureOption.CaptureCallstacks, 1);
        RenderDoc.captureOption(CaptureOption.SaveAllInitials, 1);
        RenderDoc.captureOption(CaptureOption.ApiValidation, 1);
        //Infallible a = Utils.Infallable();
        //System.out.println(a);


        EngineCreateInfo createInfo = new EngineCreateInfo("CUBE", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("BEAR test!");

        VKEngine engine = new VKEngine(createInfo);
        EventBus ev = engine.service(Services.EVENT_BUS);
//        ev.register(new DebugLogFeature());
//        ev.register(new DebugMemoryTrackingFeature());
        //engine.PROFILER = engine.service(Services.PROFILER);

        engine.start(new SceneApp("main"));
        RenderDoc.free();
    }
}