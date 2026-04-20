package com.vke.core.profiler;

import com.vke.core.profiler.service.ProfilerImpl;
import com.vke.utils.console.AnsiColors;
import com.vke.utils.console.ColorStringBuilder;
import com.vke.utils.console.PrettyTable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TablePrinter extends ProfilerPrinter<ProfilerPrinter.TableSettings> {

    public TablePrinter(ProfilerImpl.Node n, TableSettings settings) {
        super(n, Type.TABLE, settings);
    }

    @Override
    public String format() {
        ColorStringBuilder sb = new ColorStringBuilder();
        List<Category> categories = settings.getCategories();
        PrettyTable.Align[] aligns = new PrettyTable.Align[categories.size()];

        PrettyTable table = new PrettyTable();

        AtomicInteger i = new AtomicInteger(0);
        categories.forEach(c -> {
            aligns[i.getAndIncrement()] = c.getTableAlign();
            switch (c) {
                case OBJECT_TYPE -> table.column("Type");
                case NAME -> table.column("Process");
                case TIME_NS -> table.column("Time (ns)");
                case TIME_MCS -> table.column("Time (µs)");
                case TIME_MS -> table.column("Time (ms)");
                case TIME_S -> table.column("Time (s)");
                case SELF_TIME_NS -> table.column("Self Time (ns)");
                case SELF_TIME_MCS -> table.column("Self Time (µs)");
                case SELF_TIME_MS -> table.column("Self Time (ms)");
                case SELF_TIME_S -> table.column("Self Time (s)");
            }
        });

        table.setAlign(aligns);

        table.newRow();

        traverse(master, master.name, 0, sb, table);

        return table.toString();
    }

    private void traverse(ProfilerImpl.Node node, String path, int depth, ColorStringBuilder sb, PrettyTable table) {
        writeObject(node, path, sb, table);

        if (node.children == null || node.children.isEmpty())
            return;

        int maxDepth = settings.getMaxDepth();
        if (maxDepth >= 0 && depth >= maxDepth)
            return;

        for (ProfilerImpl.Node child : node.children) {
            String childPath = path + "/" + child.name;
            traverse(child, childPath, depth + 1, sb, table);
        }
    }

    private void writeObject(ProfilerImpl.Node node, String path, ColorStringBuilder sb, PrettyTable table) {
        settings.getCategories().forEach(c -> {
            if (c == Category.NAME) {
                table.column(node.color + path + AnsiColors.DEFAULT);
            } else {
                table.column(c.getFormatter().format(node));
            }
        });
        table.newRow();
    }

}
