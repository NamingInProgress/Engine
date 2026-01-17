package com.vke.api.logger;

import com.vke.utils.Colors;
import org.lwjgl.vulkan.EXTDebugUtils;

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

    public static LogLevel fromVkMessageSeverity(int severity) {
        return switch (severity) {
            case EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT -> TRACE;
            case EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT -> INFO;
            case EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT -> WARN;
            case EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT -> ERROR;
            default -> throw new IllegalStateException("Unexpected value: " + severity);
        };
    }

}
