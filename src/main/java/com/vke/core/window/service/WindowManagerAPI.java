package com.vke.core.window.service;

import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.api.window.Window;
import com.vke.api.window.WindowCreateInfo;

public class WindowManagerAPI extends ServiceAPI implements WindowManager {
    public WindowManagerAPI(ServiceImpl baseImpl) {
        super(baseImpl.getId(), baseImpl);
    }

    private WindowManager getImpl() {
        return (WindowManager) getImplementation();
    }

    @Override
    public Window createWindow(WindowCreateInfo createInfo) {
        return getImpl().createWindow(createInfo);
    }
}
