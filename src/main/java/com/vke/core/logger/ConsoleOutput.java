package com.vke.core.logger;

import com.vke.api.logger.LogFormatter;
import com.vke.api.logger.LoggerOutput;
import com.vke.api.logger.LogEvent;
import com.vke.utils.Colors;

import java.io.PrintStream;

public class ConsoleOutput implements LoggerOutput {

    static final LogFormatter defaultFormatter = (event) -> {
        Colors text = new Colors();

        text.write("[").green(event.getTimestampFormatted()).reset("]");
        text.write("[").blue(event.thread.getName()).reset("]");
        text.write("[").cyan(event.loggerName).reset("]");
        text.write("[").write(event.level.getColor()).write(event.level).reset("]: ");
        text.write(event.message);

        if (event.throwable != null) {
            text.red("\n").write(event.getThrowableFormatted());
        }

        return text.toString();
    };

    private final LogFormatter formatter;
    private final PrintStream output;

    public ConsoleOutput(PrintStream output, LogFormatter formatter) {
        this.output = output;
        this.formatter = formatter;
    }

    public ConsoleOutput(PrintStream output) {
        this(output, defaultFormatter);
    }

    @Override
    public synchronized void accept(LogEvent logEvent) {
        output.println(formatter.format(logEvent));
    }

}
