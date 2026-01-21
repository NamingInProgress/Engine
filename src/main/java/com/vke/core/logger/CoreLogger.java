package com.vke.core.logger;

import com.vke.api.logger.LoggerOutput;
import com.vke.api.logger.LogEvent;
import com.vke.api.logger.LogLevel;

import java.util.List;

public class CoreLogger implements com.vke.api.logger.Logger {

    private final String name;
    private final List<LoggerOutput> outputs;

    private LogLevel minimumLogLevel;

    CoreLogger(String name, List<LoggerOutput> outputs, LogLevel minimumLogLevel) {
        this.name = name;
        this.outputs = outputs;
        this.minimumLogLevel = minimumLogLevel;
    }

    CoreLogger(String name, List<LoggerOutput> outputs) {
        this(name, outputs, LogLevel.INFO);
    }

    public CoreLogger setMinimumLogLevel(LogLevel level) {
        this.minimumLogLevel = level;
        return this;
    }

    @Override
    public void log(LogLevel level, String message) {
        this.log(level, message, null);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        if (level.ordinal() < minimumLogLevel.ordinal()) return;

        LogEvent event = new LogEvent(level, this.name, message, throwable);
        outputs.forEach(output -> output.accept(event));
    }

}
