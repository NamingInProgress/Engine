package com.vke.test.ecs;

import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.backend.MaskMap;

public class MaskMapTest {
    public static void main(String[] args) {
        MaskMap map = new MaskMap();

        Archetype[] archetypes = new Archetype[1000];

        for (int i = 0; i < archetypes.length; i++) {
            archetypes[i] = new Archetype(null);
            map.insert(new U64ComponentMask(i), archetypes[i]);
        }

        for (int i = 0; i < archetypes.length; i++) {
            Archetype found = map.find(new U64ComponentMask(i));
            assert found == archetypes[i] : "Failed at " + i;
        }

        assert map.find(new U64ComponentMask(5000)) == null;

        System.out.println("Stress test passed!");
    }
}
