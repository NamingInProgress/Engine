package com.vke.core.event.events.lifetime;

import com.vke.api.app.App;
import com.vke.api.event.Event;

import java.util.ArrayList;
import java.util.List;

public class AppLifecycleEvents {

    public abstract static class AppEvent extends Event {

        protected App app;

        public AppEvent(App app) {
            this.app = app;
        }

        public App getApp() {
            return app;
        }
    }

    public static class PreLoad extends AppEvent {
        private final List<String> plugins;

        public PreLoad(App app) {
            super(app);
            this.plugins = new ArrayList<>();
        }

        public void addPlugin(String pluginName) {
            this.plugins.add(pluginName);
        }

        public List<String> getPlugins() { return this.plugins; }
    }

    public static class PostLoad extends AppEvent {
        public PostLoad(App app) {
            super(app);
        }
    }

    public static class PreFree extends AppEvent {
        public PreFree(App app) {
            super(app);
        }
    }

    public static class PostFree extends AppEvent {
        public PostFree(App app) {
            super(app);
        }
    }

}
