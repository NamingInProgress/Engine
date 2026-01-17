package com.vke;

import com.vke.api.logger.LogLevel;
import com.vke.core.logger.*;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        Logger log = LoggerFactory.get("VkEngine");

        log.trace("Trace");
        log.debug("Debug");
        log.info("Info");
        log.warn("Warn");
        log.error("Error");
        log.fatal("A fatal error has occurred", new NullPointerException());
    }

}
