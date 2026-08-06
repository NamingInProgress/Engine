package com.vke.test.ecs;

import com.vke.core.ecs.component.Component;
import pl.epsi.EcsComponent;

@EcsComponent
public class Position implements Component {
    public float[] x, y, z;
}
