package com.vke.impl.gameobject.convenientapi;

import com.vke.core.Context;
import com.vke.core.game.object.GameObject;
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

}
