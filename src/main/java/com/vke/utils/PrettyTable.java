package com.vke.utils;

import java.util.ArrayList;
import java.util.List;

public class PrettyTable {

    private static final String ANSI_REGEX = "\u001B\\[[;\\d]*m";

    private final List<List<String>> rows = new ArrayList<>();
    private final List<String> currentRow = new ArrayList<>();
    private Align[] alignments;

    public PrettyTable(Align... alignments) {
        this.alignments = alignments;
    }

    public PrettyTable setAlign(Align... align) {
        this.alignments = align;
        return this;
    }

    public PrettyTable column(Object data) {
        return column(data, 1);
    }

    public PrettyTable column(Object data, int colspan) {
        currentRow.add(String.valueOf(data));
        for (int i = 1; i < colspan; i++) {
            currentRow.add("");
        }
        return this;
    }

    public PrettyTable newRow() {
        rows.add(new ArrayList<>(currentRow));
        currentRow.clear();
        return this;
    }

    @Override
    public String toString() {
        if (!currentRow.isEmpty()) {
            newRow();
        }

        int cols = maxColumns();
        int[] widths = computeWidths(cols);

        StringBuilder sb = new StringBuilder();

        sb.append("┌");
        for (int c = 0; c < cols; c++) {
            sb.append("─".repeat(widths[c] + 2));
            sb.append(c == cols - 1 ? "┐\n" : "┬");
        }

        for (int r = 0; r < rows.size(); r++) {
            List<String> row = rows.get(r);

            sb.append("│");
            for (int c = 0; c < cols; c++) {
                String cell = c < row.size() ? row.get(c) : "";
                Align align = c < alignments.length ? alignments[c] : Align.LEFT;

                sb.append(" ");
                sb.append(pad(cell, widths[c], align));
                sb.append(" │");
            }
            sb.append("\n");

            if (r == 0 && rows.size() > 1) {
                sb.append("├");
                for (int c = 0; c < cols; c++) {
                    sb.append("─".repeat(widths[c] + 2));
                    sb.append(c == cols - 1 ? "┤\n" : "┼");
                }
            }
        }

        sb.append("└");
        for (int c = 0; c < cols; c++) {
            sb.append("─".repeat(widths[c] + 2));
            sb.append(c == cols - 1 ? "┘" : "┴");
        }

        return sb.toString();
    }

    private int maxColumns() {
        int max = 0;
        for (List<String> row : rows)
            max = Math.max(max, row.size());
        return max;
    }

    private int[] computeWidths(int cols) {
        int[] widths = new int[cols];

        for (List<String> row : rows) {
            for (int c = 0; c < row.size(); c++) {
                widths[c] = Math.max(widths[c], visibleLength(row.get(c)));
            }
        }
        return widths;
    }

    private String pad(String s, int width, Align align) {
        int pad = width - visibleLength(s);
        if (pad <= 0) return s;

        return align == Align.RIGHT
                ? " ".repeat(pad) + s
                : s + " ".repeat(pad);
    }

    private static int visibleLength(String s) {
        return s.replaceAll(ANSI_REGEX, "").length();
    }

    public enum Align {
        LEFT,
        RIGHT
    }

}
