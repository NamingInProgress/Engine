package com.vke.test.ecs;

import com.vke.core.ecs.services.EcsManagerImpl;
import com.vke.core.ecs.EcsCreateInfo;
import com.vke.core.ecs.component.mask.ComponentMask;

public class TheTest {
    public static void main(String[] args) {
        //init classes
        int _ = Health.ID;
        int _ = Position.ID;

        EcsCreateInfo createInfo = new EcsCreateInfo();
        EcsManagerImpl ecs = new EcsManagerImpl(null);

        int STARTUP = ecs.createCategory();

        ecs.registerQuery(STARTUP, new PrintHealthQuery());

        ComponentMask testMask = new ComponentMask(Position.ID, Health.ID);
        int amount = 1000;
        int[] entities = ecs.spawnEntities(amount, testMask, (at, base, _, idx) -> {
            Health h = at.getComponentById(Health.ID);
            h.hp[base + idx] = idx;
        });

        int first = entities[0];
        ecs.transitionEntity(first, new ComponentMask(Health.ID), null);

        ecs.runQueries(STARTUP);
    }
}
