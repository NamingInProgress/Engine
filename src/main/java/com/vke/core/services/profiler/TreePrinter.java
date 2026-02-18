package com.vke.core.services.profiler;

import com.vke.utils.ColorStringBuilder;

public class TreePrinter extends ProfilerPrinter<ProfilerPrinter.TreeSettings> {

    public TreePrinter(Profiler.Node n, TreeSettings settings) {
        super(n, Type.TREE, settings);
    }

    @Override
    public String format() {
        ColorStringBuilder sb = new ColorStringBuilder();
        sb.write(formatObject(master)).write("\n");

        int maxDepth = settings.getMaxDepth();
        if (maxDepth > 0 && master.children != null) {
            for (int i = 0; i < master.children.size(); i++) {
                boolean isLast = (i == master.children.size() - 1);
                printNode(master.children.get(i), "", isLast, 1, maxDepth, sb);
            }
        }

        return sb.toString();
    }

    private void printNode(Profiler.Node node, String prefix, boolean isLast, int depth, int maxDepth, ColorStringBuilder sb) {
        if (depth > maxDepth)
            return;

        sb.write(prefix);

        if (isLast) {
            sb.write("└─ ");
        } else {
            sb.write("├─ ");
        }

        sb.write(formatObject(node)).write("\n");

        if (node.children == null || node.children.isEmpty())
            return;

        if (depth == maxDepth)
            return;

        String childPrefix = prefix + (isLast ? "   " : "│  ");

        for (int i = 0; i < node.children.size(); i++) {
            boolean childIsLast = (i == node.children.size() - 1);
            printNode(node.children.get(i), childPrefix, childIsLast, depth + 1, maxDepth, sb);
        }
    }

    private String formatObject(Profiler.Node node) {
        StringBuilder sb = new StringBuilder();

        this.settings.getCategories().forEach(c -> {
            String formatted = c.getFormatter().format(node);
            switch (c) {
                case NAME -> sb.append(formatted).append(" (");
                case TIME_NS, TIME_MCS, TIME_MS, TIME_S -> sb.append("total: ").append(formatted);
                case SELF_TIME_NS, SELF_TIME_MS, SELF_TIME_MCS, SELF_TIME_S -> sb.append("self: ").append(formatted);
                case OBJECT_TYPE -> sb.append(formatted);
            }
            sb.append(" ");
        });

        sb.append(")");

        return sb.toString();
    }

}
