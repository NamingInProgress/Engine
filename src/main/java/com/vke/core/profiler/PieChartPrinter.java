package com.vke.core.profiler;

import com.vke.core.profiler.service.ProfilerImpl;
import com.vke.utils.console.ColoredPieChart;
import com.vke.utils.tuple.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class PieChartPrinter extends ProfilerPrinter<ProfilerPrinter.PieChartSettings> {

    public PieChartPrinter(ProfilerImpl.Node n, PieChartSettings settings) {
        super(n, Type.PIE_CHART, settings);
    }

    @Override
    public String format() {
        ColoredPieChart pc = new ColoredPieChart();

        List<ProfilerImpl.Node> atDepth = nodesAtDepth(master, settings.getDepth());
        long totalTimeAtDepth = atDepth.stream().mapToLong(ProfilerImpl.Node::getTotalTime).sum();

        atDepth.forEach(node -> {
            pc.entry(node.name, node.color, (double) node.getTotalTime() / totalTimeAtDepth);
        });

        return pc.render(10);
    }

    public List<ProfilerImpl.Node> nodesAtDepth(ProfilerImpl.Node root, int targetDepth) {
        List<ProfilerImpl.Node> result = new ArrayList<>();
        if (root == null) return result;

        Queue<Pair<ProfilerImpl.Node, Integer>> queue = new ArrayDeque<>();
        queue.add(new Pair<>(root, 0));

        while (!queue.isEmpty()) {
            Pair<ProfilerImpl.Node, Integer> pair = queue.poll();
            ProfilerImpl.Node node = pair.v1;
            int depth = pair.v2;

            if (depth == targetDepth) {
                result.add(node);
            } else if (depth < targetDepth) {
                if (node.children != null) {
                    for (ProfilerImpl.Node child : node.children) {
                        queue.add(new Pair<>(child, depth + 1));
                    }
                }
            }
        }

        return result;
    }

}
