package com.vke.api.event;

import com.vke.api.services2.Service;

import java.lang.invoke.MethodHandle;
import java.util.Objects;

public interface EventBus extends Service {

    void register(EventListener instance);
    void remove(EventListener instance);
    /**
     * @return true when success, false when canceled
     */
    boolean fire(Event event);

    record CallableHandler(EventListener instance, MethodHandle methodHandle) {

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            CallableHandler that = (CallableHandler) o;
            return Objects.equals(instance, that.instance);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(instance);
        }
    }

}
