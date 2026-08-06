package com.vke.test.ecs;

import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.component.mask.ComponentMask;

public class PrintHealthQuery implements Query {
    @Override
    public ComponentMask getMask() {
        return new ComponentMask(Health.ID);
    }

    @Override
    public void execute(Archetype at, int i0, int i1) {
        Health h = at.getComponentById(Health.ID);
        for (int i = i0; i < i1; i++) {
            System.out.println(h.hp[i]);
        }
    }
}
