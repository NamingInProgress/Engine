package com.vke.api.rendering.abstraction.renderer;

public interface IntEnum {

    int getIntVal();

    default boolean areTheIntegersSorted() { return false; }

    @SuppressWarnings("all")
    static <T extends IntEnum> T fromInt(T[] values, int handle) {
        int left = 0, right = values.length - 1;
        if (values.length == 0) throw new IllegalArgumentException("Not a value to choose of available at value array");

        T template = values[0];
        if (template.areTheIntegersSorted()) {
            while (true) {
                int mid = left + (right - left) / 2;
                T cand = values[mid];
                int key = cand.getIntVal();
                if (handle < key) {
                    right = mid - 1;
                } else if (handle > key) {
                    left = mid + 1;
                } else {
                    return cand;
                }
                if (left > right) break;
            }
        } else {
            for (T value : values) {
                if (value.getIntVal() == handle) {
                    return value;
                }
            }
        }
        String className = values[0].getClass().getName();
        throw new IllegalArgumentException("Illegal int value (" + handle + ") for IntEnum: " + className);
    }
}
