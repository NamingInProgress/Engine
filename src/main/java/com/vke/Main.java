package com.vke;

import com.vke.api.logger.LogLevel;
import com.vke.core.logger.*;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        LoggerFactory.init(new LoggerConfig(LogLevel.TRACE), List.of(new ConsoleOutput()));
        Logger log = LoggerFactory.get("VkEngine");

        log.trace("Trace");
        log.debug("Debug");
        log.info("Info");
        log.warn("Warn");
        log.error("Error");
        log.fatal("Fatal");
    }

}
