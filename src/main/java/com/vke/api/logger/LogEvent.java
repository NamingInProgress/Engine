package com.vke.api.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

public final class LogEvent {

    public final Instant timestamp;
    public final LogLevel level;
    public final String loggerName;
    public final String message;
    public final Throwable throwable;
    public final Thread thread;

    public LogEvent(LogLevel level, String loggerName, String message, Throwable throwable) {
        this.timestamp = Instant.now();
        this.level = level;
        this.loggerName = loggerName;
        this.message = message;
        this.throwable = throwable;
        this.thread = Thread.currentThread();
    }

    public String getThrowableFormatted() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        this.throwable.printStackTrace(pw);
        pw.flush();

        return sw.toString();
    }

}
