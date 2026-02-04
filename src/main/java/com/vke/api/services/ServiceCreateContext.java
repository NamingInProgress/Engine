package com.vke.api.services;

import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;

public record ServiceCreateContext(VKEngine engine, EngineCreateInfo engineCreateInfo) {
}
