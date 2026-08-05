package com.vke.test.ecs;

import com.vke.core.ecs.component.Component;
import pl.epsi.EcsComponent;

@EcsComponent
public class Health implements Component {
    public int[] hp;
}
