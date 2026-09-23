package com.vke.core.rendering.debug.service;

import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;

public class DebugManagerAPI extends ServiceAPI implements DebugManager {
    public DebugManagerAPI(String id, ServiceImpl baseImpl) {
        super(id, baseImpl);
    }
}
