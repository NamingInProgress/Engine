package com.vke.api.vkz;

public record ProgressReport(String currentFile, int fileCount, int current, int currentSize) {
    public interface Listener {
        void onNewProgress(ProgressReport report);

        static Listener silent() {
            return Silent.getInstance();
        }

        class Silent implements Listener {
            private static Silent intance;

            private Silent() {
            }

            public static Silent getInstance() {
                if (intance == null) intance = new Silent();
                return intance;
            }

            @Override
            public void onNewProgress(ProgressReport report) {
                //pssst! i am silent!
            }
        }
    }
}
