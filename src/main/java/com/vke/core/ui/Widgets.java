package com.vke.core.ui;

import com.vke.api.logger.Logger;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.ui.blueprint.One;
import com.vke.core.ui.signal.Eval;
import com.vke.core.ui.signal.Signal;
import com.vke.core.ui.signal.SignalTracker;
import com.vke.utils.Utils;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public final class Widgets {
    public static SignalTracker track(Signal<?>... signals) {
        return SignalTracker.track(signals);
    }

    public record Args(SignalTracker tracker, List<Arg> args) {}
    public record Arg(String name, Object value) {}

    public static class ArgIter implements Iter<Arg> {
        private static final Logger LOG = LoggerFactory.get("Ui Blueprint");

        private final Object[] args;
        private int i;

        public ArgIter(Object[] args, int i) {
            this.args = args;
            this.i = i;
        }

        @Override
        public @NotNull Option<Arg> next() {
            if (i >= args.length) return Option.none();
            Object name = args[i++];
            if (i >= args.length) {
                LOG.warn("Unused argument '%s' at the end of argument list! ignoring it...", name);
                return Option.none();
            }
            if (name instanceof String nameStr) {
                Object arg = args[i++];
                return Option.some(new Arg(nameStr, arg));
            }

            throw new MalformedArgumentsException();
        }
    }

    public static Args args(Object[] args) {
        if (args.length > 0) {
            SignalTracker tracker = null;
            List<Arg> list = new LinkedList<>();

            for (int i = 0; i < args.length; i++) {
                Object key = args[i];
                if (key instanceof String name) {
                    if (!Utils.verifyArrayIndex(i + 1, args)) {
                        throw new MalformedArgumentsException();
                    }

                    Object value = args[i];
                    list.add(new Arg(name, value));
                } else if (key instanceof SignalTracker tr) {
                    if (tracker == null) {
                        tracker = tr;
                    } else {
                        tracker.append(tr);
                    }
                } else if (key instanceof Signal<?> signal) {
                    if (tracker == null) {
                        tracker = track(signal);
                    } else {
                        tracker.push(signal);
                    }
                } else if (key instanceof Object[] additional) {
                    args = Utils.mergeTs(args, additional);
                }
            }

            return new Args(tracker, list);
        }
        return new Args(null, List.of());
    }

    public static <T> Eval<T> eval(Eval<T> func) {
        return func;
    }

    public static One button(Object... args) {
        Args a = args(args);
    }
}
