package com.vke.api.vkz;

import java.io.InputStream;

public interface VkzFileHandle {
    InputStream getInputStream();

    String getName();

    int getSize();

    VkzEditor edit();
}
