package com.vke.core.profiler;

import com.vke.utils.console.AnsiColors;
import com.vke.utils.console.PrettyTable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class ProfilerPrinter<T extends ProfilerPrinter.Settings> {

    protected final Profiler.Node master;
    protected final Type type;
    protected final T settings;

    public ProfilerPrinter(Profiler.Node n, Type type, T settings) {
        this.settings = settings == null ? Settings.fromTypeDefault(type) : settings;
        this.type = type;
        this.master = n;
    }

    @SuppressWarnings("unchecked")
    public static <U extends Settings> ProfilerPrinter<U> getPrinter(Profiler.Node n, Type type, U settings) {
        return (ProfilerPrinter<U>) switch (type) {
            case TREE -> new TreePrinter(n, (TreeSettings) settings);
            case TABLE -> new TablePrinter(n, (TableSettings) settings);
            case PIE_CHART -> new PieChartPrinter(n, (PieChartSettings) settings);
        };
    }

    public abstract String format();

    private interface IntEnum {
        int asInt();
        default boolean isInMask(int mask) {
            return (mask & asInt()) != 0;
        }
    }

    @FunctionalInterface
    public interface ProfilerFormatter {
        String internalFormat(Profiler.Node node);
        default String format(Profiler.Node node) { return internalFormat(node) + AnsiColors.DEFAULT; }
    }

    public enum Type implements IntEnum {

        TREE(1 << 0),
        TABLE(1 << 1),
        PIE_CHART(1 << 2);

        private final int value;

        Type(int value) {
            this.value = value;
        }

        @Override
        public int asInt() { return this.value; }

    }

    public enum Category implements IntEnum {

        OBJECT_TYPE    (1 << 0, PrettyTable.Align.LEFT, (node) -> AnsiColors.CYAN + "(" + node.getObjectType() + ")"),
        NAME           (1 << 1, PrettyTable.Align.LEFT, (node) -> node.color + node.name),
        TIME_NS        (1 << 2, PrettyTable.Align.RIGHT, (node) -> AnsiColors.GREEN + node.getTotalTime() + "ns"),
        TIME_MCS       (1 << 3, PrettyTable.Align.RIGHT, (node) -> AnsiColors.GOLD + node.getTotalTime() / 1_000 + "µs"),
        TIME_MS        (1 << 4, PrettyTable.Align.RIGHT, (node) -> AnsiColors.YELLOW + node.getTotalTime() / 1_000_000 + "ms"),
        TIME_S         (1 << 5, PrettyTable.Align.RIGHT, (node) -> AnsiColors.RED + node.getTotalTime() / 1_000_000_000 + "s"),
        SELF_TIME_NS   (1 << 6, PrettyTable.Align.RIGHT, (node) -> AnsiColors.BLUE + node.getSelfTime() + "ns"),
        SELF_TIME_MCS  (1 << 7, PrettyTable.Align.RIGHT, (node) -> AnsiColors.BLUE + node.getSelfTime() / 1_000 + "µs"),
        SELF_TIME_MS   (1 << 8, PrettyTable.Align.RIGHT, (node) -> AnsiColors.BLUE + node.getSelfTime() / 1_000_000 + "ms"),
        SELF_TIME_S    (1 << 9, PrettyTable.Align.RIGHT, (node) -> AnsiColors.BLUE + node.getSelfTime() / 1_000_000_000 + "s");

        private final int value;
        private final ProfilerFormatter formatter;
        private final PrettyTable.Align tableAlign;

        Category(int value, PrettyTable.Align align, ProfilerFormatter formatter) {
            this.value = value;
            this.tableAlign = align;
            this.formatter = formatter;
        }

        @Override
        public int asInt() { return this.value; }

        public ProfilerFormatter getFormatter() {
            return formatter;
        }

        public PrettyTable.Align getTableAlign() {
            return tableAlign;
        }
    }

    public static class Settings {

        private int categoriesMask;

        public <T extends Settings> T withCategories(Category... categories) {
            for (Category category : categories) {
                categoriesMask |= category.asInt();
            }
            return (T) this;
        }

        public List<Category> getCategories() {
            List<Category> ret = new ArrayList<>();

            for (Category category : Category.values()) {
                if (category.isInMask(this.categoriesMask)) ret.add(category);
            }

            ret.sort(Comparator.comparingInt(Enum::ordinal));
            return ret;
        }

        @SuppressWarnings("unchecked")
        public static <T extends Settings> T fromTypeDefault(Type type) {
            return (T) switch (type) {
                case TREE -> defaultTree();
                case TABLE -> defaultTable();
                case PIE_CHART -> defaultPieChart();
            };
        }

        public static TreeSettings defaultTree() {
            return new TreeSettings().withCategories(Category.NAME, Category.TIME_MCS, Category.SELF_TIME_MCS);
        }
        public static TableSettings defaultTable() {
            return new TableSettings().withCategories(Category.OBJECT_TYPE, Category.NAME,
                    Category.SELF_TIME_NS, Category.SELF_TIME_MS, Category.SELF_TIME_MCS,
                    Category.TIME_NS, Category.TIME_MS, Category.TIME_MCS);
        }
        public static PieChartSettings defaultPieChart() {
            return new PieChartSettings();
        }

    }

    public static class TreeSettings extends Settings {

        private int maxDepth = 2;

        public void setMaxDepth(int depth) { this.maxDepth = depth; }
        public int getMaxDepth() { return this.maxDepth; }

    }

    public static class TableSettings extends TreeSettings {

        public TableSettings() { setMaxDepth(Integer.MAX_VALUE); }

    }

    public static class PieChartSettings extends Settings {

        private int depth = 1;

        public void setDepth(int depth) { this.depth = depth; }
        public int getDepth() { return this.depth; }

    }

}
