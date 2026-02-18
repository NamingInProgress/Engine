package com.vke.core.services.profiler;

import com.vke.utils.AnsiColors;
import com.vke.utils.ColoredPieChart;
import com.vke.utils.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class PieChartPrinter extends ProfilerPrinter<ProfilerPrinter.PieChartSettings> {

    public PieChartPrinter(Profiler.Node n, PieChartSettings settings) {
        super(n, Type.PIE_CHART, settings);
    }

    @Override
    public String format() {
        ColoredPieChart pc = new ColoredPieChart();

        List<Profiler.Node> atDepth = nodesAtDepth(master, settings.getDepth());
        long totalTimeAtDepth = atDepth.stream().mapToLong(Profiler.Node::getTotalTime).sum();

        atDepth.forEach(node -> {
            pc.entry(node.name, node.color, (double) node.getTotalTime() / totalTimeAtDepth);
        });

        return pc.render(10);
    }

    public List<Profiler.Node> nodesAtDepth(Profiler.Node root, int targetDepth) {
        List<Profiler.Node> result = new ArrayList<>();
        if (root == null) return result;

        Queue<Pair<Profiler.Node, Integer>> queue = new ArrayDeque<>();
        queue.add(new Pair<>(root, 0));

        while (!queue.isEmpty()) {
            Pair<Profiler.Node, Integer> pair = queue.poll();
            Profiler.Node node = pair.v1;
            int depth = pair.v2;

            if (depth == targetDepth) {
                result.add(node);
            } else if (depth < targetDepth) {
                if (node.children != null) {
                    for (Profiler.Node child : node.children) {
                        queue.add(new Pair<>(child, depth + 1));
                    }
                }
            }
        }

        return result;
    }

}
