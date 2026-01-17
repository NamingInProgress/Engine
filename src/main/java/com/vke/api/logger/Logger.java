package com.vke.api.logger;

public interface Logger {

    void log(LogLevel level, String message);
    void log(LogLevel level, String message, Throwable throwable);

    default void formatLog(LogLevel level, String message, Object... args) {
        log(level, String.format(message, args));
    }

    default void formatLog(LogLevel level, String message, Throwable t, Object... args) {
        log(level, String.format(message, args), t);
    }

    default void trace(String message, Object... args) { this.formatLog(LogLevel.TRACE, message, args); }
    default void trace(String message, Throwable t, Object... args) { this.formatLog(LogLevel.TRACE, message, t, args); }

    default void debug(String message, Object... args) { this.formatLog(LogLevel.DEBUG, message, args); }
    default void debug(String message, Throwable t, Object... args) { this.formatLog(LogLevel.DEBUG, message, t, args); }

    default void info(String message, Object... args) { this.formatLog(LogLevel.INFO, message, args); }
    default void info(String message, Throwable t, Object... args) { this.formatLog(LogLevel.INFO, message, t, args); }

    default void warn(String message, Object... args) { this.formatLog(LogLevel.WARN, message, args); }
    default void warn(String message, Throwable t, Object... args) { this.formatLog(LogLevel.WARN, message, t, args); }

    default void error(String message, Object... args) { this.formatLog(LogLevel.ERROR, message, args); }
    default void error(String message, Throwable t, Object... args) { this.formatLog(LogLevel.ERROR, message, t, args); }

    default void fatal(String message, Object... args) { this.formatLog(LogLevel.FATAL, message, args); }
    default void fatal(String message, Throwable t, Object... args) { this.formatLog(LogLevel.FATAL, message, t, args); }

}
