package com.vke.api.vkz;

import java.io.InputStream;

public interface VkzFileHandle {
    /**
     * <h2>Attention!</h2>
     * You MUST close the input stream, no matter what! Otherwise the other IO operations will wait forever.
     * @return the input stream to this file
     */
    InputStream getInputStream();

    String getName();

    int getSize();

    VkzEditor edit();

    boolean isLocked();
}
