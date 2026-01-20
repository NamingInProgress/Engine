package com.vke.core.logger;

import com.vke.api.logger.LogFormatter;
import com.vke.api.logger.LoggerOutput;
import com.vke.api.logger.LogEvent;
import com.vke.utils.Colors;

import java.io.FileWriter;
import java.io.IOException;

public class FileOutput implements LoggerOutput {

    static final LogFormatter defaultFormatter = (event) -> {
        Colors text = new Colors();

        text.write("[%s]".formatted(event.getTimestampFormatted()));
        text.write("[%s]".formatted(event.thread.getName()));
        text.write("[%s]".formatted(event.loggerName));
        text.write("[%s]: ".formatted(event.level));
        text.write(event.message);

        if (event.throwable != null) {
            text.red("\n").write(event.getThrowableFormatted());
        }

        return text.toString();
    };

    private final LogFormatter formatter;
    private final FileWriter writer;

    public FileOutput(String path, LogFormatter formatter) throws IOException {
        this.formatter = formatter;
        this.writer = new FileWriter(path, true);
    }

    public FileOutput(String path) throws IOException {
        this(path, defaultFormatter);
    }

    @Override
    public synchronized void accept(LogEvent event) {
        try {
            writer.write(formatter.format(event));
            writer.write("\n");
            writer.flush();
        } catch (IOException ignored) {}
    }

}
