#!/bin/bash

cd /home/tolek/Projects/VKEngine

exec /home/tolek/.jdks/jbr-25.0.1/bin/java \
    -Dorg.lwjgl.system.stackSize=256 \
    -cp "target/test-classes:target/classes:$(find ~/.m2/repository -name '*.jar' | tr '\n' ':')" \
    com.vke.test.rendering.Main
