package com.vke.impl.gameobject.convenientapi;

import com.vke.core.Context;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.game.object.GameObject;
import com.vke.core.services2.Services;
import com.vke.impl.ecs.mesh.StaticMeshC;
import com.vke.impl.ecs.text.BasicTextC;
import com.vke.impl.gameobject.EmptyGameObject;

public class GameObjects {

    private static GameObject spawn(Context context) {
        GameObject go = new EmptyGameObject(context);
        go.spawn();
        return go;
    }

    public static GameObject staticMesh(Context context) {
        GameObject go = spawn(context);
        go.addComponents(StaticMeshC.ID);
        return go;
    }

    public static GameObject basicText(Context context) {
        GameObject go = spawn(context);
        go.addComponents(BasicTextC.ID);
        return go;
    }

    public static void destroyBatch(GameObject... gameObjects) {
        if (gameObjects.length == 0) return;
        Context context = gameObjects[0].getContext();
        EcsManager ecs = context.service(Services.ECS);
        int[] ids = new int[gameObjects.length];
        for (int i = 0; i < gameObjects.length; i++) {
            GameObject gameObject = gameObjects[i];
            ids[i] = gameObject.entityId();
        }
        ecs.destroyEntities(ids);
    }
}
