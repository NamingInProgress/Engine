package com.vke.utils.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ExceptionChain {
    private final List<Throwable> thrown;

    public ExceptionChain() {
        this.thrown = new ArrayList<>();
    }

    public void onThrow(Throwable e) {
        thrown.add(e);
    }

    public <T extends Throwable> void throwIfNecessary(Function<String, T> creator) throws T {
        if (!thrown.isEmpty()) {
            StringBuilder builder = new StringBuilder("There were exceptions:").append(System.lineSeparator());
            for (Throwable e : thrown) {
                builder.append("\t-> ").append(e.getMessage()).append(System.lineSeparator());
            }

            throw creator.apply(builder.toString());
        }
    }
}
