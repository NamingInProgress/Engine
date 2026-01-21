package com.vke.api.logger;

public interface LoggerOutput {

    void accept(LogEvent logEvent);

}
