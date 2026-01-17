package com.vke.api.logger;

public interface Logger {

    void log(LogLevel level, String message);
    void log(LogLevel level, String message, Throwable throwable);

    default void trace(String message) { this.log(LogLevel.TRACE, message); }
    default void trace(String message, Throwable t) { this.log(LogLevel.TRACE, message, t); }

    default void debug(String message) { this.log(LogLevel.DEBUG, message); }
    default void debug(String message, Throwable t) { this.log(LogLevel.DEBUG, message, t); }

    default void info(String message) { this.log(LogLevel.INFO, message); }
    default void info(String message, Throwable t) { this.log(LogLevel.INFO, message, t); }

    default void warn(String message) { this.log(LogLevel.WARN, message); }
    default void warn(String message, Throwable t) { this.log(LogLevel.WARN, message, t); }

    default void error(String message) { this.log(LogLevel.ERROR, message); }
    default void error(String message, Throwable t) { this.log(LogLevel.ERROR, message, t); }

    default void fatal(String message) { this.log(LogLevel.FATAL, message); }
    default void fatal(String message, Throwable t) { this.log(LogLevel.FATAL, message, t); }

}
