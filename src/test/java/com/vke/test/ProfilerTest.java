package com.vke.test;

import com.vke.core.profiler.Profiler;
import com.vke.core.profiler.ProfilerPrinter;
import com.vke.utils.console.AnsiColors;

public class ProfilerTest {

    public static void main(String[] args) {
        Profiler profiler = new Profiler();
        profiler.withDisplayTypes(ProfilerPrinter.Type.PIE_CHART);
        var s = new ProfilerPrinter.PieChartSettings();
        s.setDepth(2);
        profiler.setSettingsForType(ProfilerPrinter.Type.PIE_CHART, s);
        profiler.beginFrame();

        profiler.begin("Render", AnsiColors.RED);
        profiler.begin("Start Frame");
        profiler.end();

        profiler.begin("App", AnsiColors.CYAN);
        profiler.begin("idk");
        profiler.end();
        profiler.end();

        profiler.begin("End Frame", AnsiColors.GOLD);
        profiler.end();

        profiler.end();
        profiler.begin("Physics", AnsiColors.BLUE);
        profiler.begin("Idk v2");
        profiler.end();
        profiler.end();

        profiler.endFrame();
    }

}
