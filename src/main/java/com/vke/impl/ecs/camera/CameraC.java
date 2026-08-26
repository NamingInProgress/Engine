package com.vke.impl.ecs.camera;

import com.vke.core.color.Color;
import com.vke.core.ecs.component.Component;
import pl.epsi.EcsComponent;

@EcsComponent
public class CameraC implements Component {

    public float[] fov;
    public float[] nearPlane;
    public float[] farPlane;
    public float[] clearR, clearG, clearB, clearA;
    public int[] renderTexture;
    public float[] zoom;


    /*

                                     _____  _____
                                <     `/     |
                                 >          (
                                |   _     _  |
                                |  |_) | |_) |
                                |  | \ | |   |
                                |            |
                 ______.______%_|            |__________  _____
               _/                                       \|     |
              |               COOL OPTIMIZATIOn              <
              |_____.-._________              ____/|___________|
                                | * fi/ll/in |
                                | + 19/10/97 |
                                |            |
                                |            |
                                |   _        <
                                |__/         |
                                 / `--.      |
                               %|            |%
                           |/.%%|          -< @%%%
                           `\%`@|     v      |@@%@%%    - mfj
                         .%%%@@@|%    |    % @@@%%@%%%%
                    _.%%%%%%@@@@@@%%_/%\_%@@%%@@@@@@@%%%%%%

    */


    //used fields by the projection matrix
    public float[] p1, p2, p3, p4, p5, p6;
    public boolean[] isOrtho;

    @Override
    public void initialize(int i) {
        initialize(i, 90, 0.1f, 1000f, Color.VKE);
    }

    public void initialize(int i, float fov, float nearPlane, float farPlane, Color color) {
        this.fov[i] = fov;
        this.nearPlane[i] = nearPlane;
        this.farPlane[i] = farPlane;
        setClearColor(i, color);
        this.renderTexture[i] = 0;
        this.isOrtho[i] = false;
        this.zoom[i] = 1.0f;
    }

    public void setClearColor(int i, Color color) {
        this.clearR[i] = color.x;
        this.clearG[i] = color.y;
        this.clearB[i] = color.z;
        this.clearA[i] = color.w;
    }

    public Color getClearColor(int i) {
        return new Color(this.clearR[i], this.clearG[i], this.clearB[i], this.clearA[i]);
    }

}
