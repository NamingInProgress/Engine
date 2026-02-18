package com.vke.core.services;

import com.vke.api.logger.Logger;
import com.vke.api.services.Service;
import com.vke.core.logger.LoggerFactory;
import com.vke.utils.ColorStringBuilder;
import com.vke.utils.StringTable;
import com.vke.utils.Tripple;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;

public class PerformanceStatistics extends Service {

    private static final Logger logger = LoggerFactory.get("Perf Stats");

    protected PerformanceStatistics() {
        super("psts");
    }

    private Category frame;
    private final Stack<Category> cat = new Stack<>();

    public void beginFrame() {
        cat.clear();
        frame = new Category("Frame");
        cat.push(frame);
    }

    public void endFrame() {
        //if (!cat.peek().equals(frame)) logger.warn("Unclosed category!");
        if (cat.size() > 1) return;
        frame.end();

        ColorStringBuilder sb = new ColorStringBuilder();
        StringTable table = new StringTable();

        table.tr();
        table.td(sb.clear().gray("Process"));
        table.td(sb.clear().gray("Time (ns)"));
        table.td(sb.clear().gray("Time (ms)"));
        table.tr();

        formatCategory("", frame, sb, table);

        sb.reset();

        char[] paddings = {'r', 'l', 'l'};
        //logger.info("Performance Statistic: \n" +  table.construct(paddings));
    }

    private void formatObject(String parentName, String col, ColorStringBuilder sb, PerformanceObject obj, StringTable table) {
        table.td(sb.clear().line(col, parentName).write("/").write(obj.name));
        table.td(sb.clear().line(col, obj.time).write("ns"));
        table.td(sb.clear().line(col, obj.time / 1_000_000).write("ms"));
        table.tr();
    }

    private void formatCategory(String parentName, Category category, ColorStringBuilder sb, StringTable table) {
        String col = ColorStringBuilder.DEFAULT;

        table.td(sb.clear().line(col, category.name));
        table.td(sb.clear().line(col, category.time).write("ns"));
        table.td(sb.clear().line(col, category.time / 1_000_000).write("ms"));
        table.tr();

        category.objects.forEach((name, obj) -> {
            if (obj instanceof Category c) {
                formatCategory(parentName.isEmpty() ? name : parentName + "/" + name, c, sb, table);
            } else {
                formatObject(parentName, col, sb, obj, table);
            }
        });
    }

    public void category(String name) {
        Category c = new Category(name);
        cat.peek().put(c);
        cat.push(c);
    }

    public void endCategory() {
        if (cat.isEmpty()) logger.warn("Could not end category because the stack is empty!");
        cat.pop();
    }

    public void record(String name) {
        cat.peek().put(new PerformanceObject(name));
    }

    public void end(String name) {
        cat.peek().end(name);
    }

    public void end(PerformanceObject obj) {
        cat.peek().end(obj);
    }

    public static class PerformanceObject {

        public String name;
        public long startTime;
        public long time;

        public PerformanceObject(String name) {
            this.name = name;
            this.startTime = System.nanoTime();
        }

        public void end() {
            this.time = System.nanoTime() - startTime;
        }

    }

    public static class Category extends PerformanceObject {

        private final LinkedHashMap<String, PerformanceObject> objects = new LinkedHashMap<>();

        public Category(String name) {
            super(name);
        }

        public void put(PerformanceObject obj) {
            objects.put(obj.name, obj);
        }

        public void end(PerformanceObject obj) {
            objects.get(obj.name).end();
        }

        public void end(String name) {
            objects.get(name).end();
        }

    }

    @Override
    protected List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {}

}
