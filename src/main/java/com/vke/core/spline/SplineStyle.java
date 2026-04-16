package com.vke.core.spline;

import com.vke.core.color.RgbColor;

public class SplineStyle {
    public boolean filled;
    public float strokeWidth;
    public RgbColor fillColor;
    public RgbColor strokeColor;

    public SplineStyle() {
        this.filled = false;
        this.strokeWidth = 1;
        this.fillColor = null;
        this.strokeColor = RgbColor.BLACK;
    }
}
