package com.vke.core.file.ogg.vorbis.setup;

public class FloorConfig {
    public final int type;
    public final Object floor;

    public FloorConfig(int type, Object floor) {
        this.type = type;
        this.floor = floor;
    }

    @Override
    public String toString() {
        return "FloorConfig{" +
                "type=" + type +
                ", floor=" + floor +
                '}';
    }
}
