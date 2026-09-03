package com.vke.impl.ecs.camera;

import com.vke.core.color.RgbColor;
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
                                | * 26/08/26 |
                                | + 26/08/26 |
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

    public boolean[] isOrtho;

    @Override
    public void initialize(int i) {
        initialize(i, 90, 0.1f, 1000f, RgbColor.VKE);
    }

    public void initialize(int i, float fov, float nearPlane, float farPlane, RgbColor color) {
        this.fov[i] = fov;
        this.nearPlane[i] = nearPlane;
        this.farPlane[i] = farPlane;
        setClearColor(i, color);
        this.renderTexture[i] = 0;
        this.isOrtho[i] = false;
        this.zoom[i] = 1.0f;
    }

    public void setClearColor(int i, RgbColor color) {
        this.clearR[i] = color.r();
        this.clearG[i] = color.g();
        this.clearB[i] = color.b();
        this.clearA[i] = color.a();
    }

}
