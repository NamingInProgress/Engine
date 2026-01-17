package com.vke.core.logger;

import com.vke.api.logger.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class SOUT {
    public static final String TAG = "SOUT";

    private static final StringBuilder outBuffer = new StringBuilder();
    private static final StringBuilder errBuffer = new StringBuilder();

    private static final Object outLock = new Object();
    private static final Object errLock = new Object();

    private SOUT() {}

    public static void redirect(Logger logger) {
        System.setOut(createPrintStream(logger, true));
        System.setErr(createPrintStream(logger, false));
    }

    private static PrintStream createPrintStream(Logger logger, boolean isOut) {
        return new PrintStream(new OutputStream() {

            @Override
            public void write(int b) {
                write(new byte[]{(byte) b}, 0, 1);
            }

            @Override
            public void write(byte[] b, int off, int len) {
                String text = new String(b, off, len, StandardCharsets.UTF_8);

                StringBuilder buffer = isOut ? outBuffer : errBuffer;
                Object lock = isOut ? outLock : errLock;

                synchronized (lock) {
                    buffer.append(text);

                    int newline;
                    while ((newline = buffer.indexOf("\n")) >= 0) {
                        String line = buffer.substring(0, newline);

                        if (line.endsWith("\r")) {
                            line = line.substring(0, line.length() - 1);
                        }

                        buffer.delete(0, newline + 1);

                        if (!line.isEmpty()) {
                            if (isOut) {
                                logger.info(line);
                            } else {
                                logger.error(line);
                            }
                        }
                    }
                }
            }

            @Override
            public void flush() {
                StringBuilder buffer = isOut ? outBuffer : errBuffer;
                Object lock = isOut ? outLock : errLock;

                synchronized (lock) {
                    if (buffer.length() > 0) {
                        String remaining = buffer.toString();
                        buffer.setLength(0);

                        if (!remaining.isEmpty()) {
                            if (isOut) {
                                logger.info(remaining);
                            } else {
                                logger.error(remaining);
                            }
                        }
                    }
                }
            }

        }, true, StandardCharsets.UTF_8);
    }
}
