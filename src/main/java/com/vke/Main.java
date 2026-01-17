package com.vke;

import com.vke.core.logger.*;

public class Main {

    public static void main(String[] args) {
        Logger log = LoggerFactory.get("VkEngine");

        String name = "guy";
        log.trace("Trace %s", name);
        log.debug("Debug");
        log.info("Info");
        log.warn("Warn");
        log.error("Error");
        log.fatal("A fatal error has occurred", new NullPointerException());
    }

}
