package com.vke.api.logger;

import com.vke.utils.Colors;

public enum LogLevel {

    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    FATAL;

    LogLevel() {}

    public String getColor() {
        return switch(this) {
            case TRACE -> Colors.BLUE;
            case DEBUG -> Colors.CYAN;
            case INFO -> Colors.GREEN;
            case WARN -> Colors.YELLOW;
            case ERROR, FATAL -> Colors.RED;
        };
    }

}
