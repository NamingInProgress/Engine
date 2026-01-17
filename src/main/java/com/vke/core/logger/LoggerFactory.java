package com.vke.core.logger;

import com.vke.api.logger.LoggerOutput;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LoggerFactory {

    private static final Map<String, Logger> LOGGERS = new ConcurrentHashMap<>();

    private static LoggerConfig config;
    private static List<LoggerOutput> sharedOutputs;

    private LoggerFactory() {}

    public static void init(LoggerConfig config, List<LoggerOutput> sharedOutputs) {
        LoggerFactory.config = config;
        LoggerFactory.sharedOutputs = sharedOutputs;
    }

    public static Logger get(String name) {
        return LOGGERS.computeIfAbsent(name, LoggerFactory::createLogger);
    }

    public static Logger createLogger(String name) {
        return new Logger(name, sharedOutputs, config.defaultLevel());
    }

}
