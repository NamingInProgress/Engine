package com.vke.core.logger;

import com.vke.api.logger.LogLevel;
import com.vke.api.logger.LoggerOutput;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LoggerFactory {

    static {
        init(new LoggerConfig(LogLevel.TRACE), List.of(new ConsoleOutput(System.out)));
    }

    private static final Map<String, CoreLogger> LOGGERS = new ConcurrentHashMap<>();

    private static LoggerConfig config;
    private static List<LoggerOutput> sharedOutputs;

    private LoggerFactory() {}

    public static void init(LoggerConfig config, List<LoggerOutput> sharedOutputs) {
        LoggerFactory.config = config;
        LoggerFactory.sharedOutputs = sharedOutputs;
    }

    public static CoreLogger get(String name) {
        return LOGGERS.computeIfAbsent(name, LoggerFactory::createLogger);
    }

    private static CoreLogger createLogger(String name) {
        return new CoreLogger(name, sharedOutputs, config.defaultLevel());
    }

}
