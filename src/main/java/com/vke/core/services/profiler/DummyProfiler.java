package com.vke.core.services.profiler;

public class DummyProfiler extends Profiler {

    @Override
    public void defaultSettings(ProfilerPrinter.Type type) {}

    @Override
    public void setSettingsForType(ProfilerPrinter.Type type, ProfilerPrinter.Settings settings) {}

    @Override
    public void disableDisplayTypes(ProfilerPrinter.Type... type) {}

    @Override
    public void withDisplayTypes(ProfilerPrinter.Type... type) {}

    @Override
    public void end() {}

    @Override
    public void begin(String name, String color) {}

    @Override
    public void begin(String name) {}

    @Override
    public void endFrame() {}

    @Override
    public void beginFrame() {}

}
