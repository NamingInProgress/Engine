package com.vke.core.window.service;

import com.vke.api.services2.PinnedService;
import com.vke.api.window.Window;
import com.vke.api.window.WindowCreateInfo;

public interface WindowManager extends PinnedService {
    Window createWindow(WindowCreateInfo createInfo);
}
