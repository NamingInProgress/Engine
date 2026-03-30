package com.vke.utils.console;

import java.util.ArrayList;
import java.util.List;

public class ColoredPieChart {

    private static class Entry {
        String name;
        String color;
        double startAngle;
        double endAngle;
    }

    private final List<Entry> entries = new ArrayList<>();

    public void entry(String n, String c, double percent) {
        entries.add(new Entry() {{
            this.name = n;
            this.color = c;
            this.endAngle = percent * Math.PI * 2;
        }});
    }

    public String render(int radius) {
        double current = 0;
        for (Entry e : entries) {
            e.startAngle = current;
            current += e.endAngle;
            e.endAngle = current;
        }

        StringBuilder sb = new StringBuilder();
        int size = radius * 2 + 1;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {

                double dx = x - radius;
                double dy = radius - y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist > radius) {
                    sb.append("  ");
                    continue;
                }

                double angle = Math.atan2(dy, dx);
                if (angle < 0) angle += Math.PI * 2;

                Entry e = findEntry(angle);
                sb.append(e.color).append("██");
            }
            sb.append(AnsiColors.DEFAULT).append('\n');
        }

        sb.append("\n");
        for (Entry e : entries) {
            sb.append(e.color)
                    .append("██ ")
                    .append(e.name)
                    .append(" ")
                    .append(String.format("%.1f",
                            (e.endAngle - e.startAngle) / (2 * Math.PI) * 100))
                    .append("%%")
                    .append(AnsiColors.DEFAULT)
                    .append('\n');
        }

        return sb.toString();
    }

    private Entry findEntry(double angle) {
        for (Entry e : entries) {
            if (angle >= e.startAngle && angle < e.endAngle)
                return e;
        }
        return entries.get(entries.size() - 1);
    }

}
