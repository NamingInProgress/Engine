package com.vke.api.logger;

@FunctionalInterface
public interface LogFormatter {

    String format(LogEvent event);

}
