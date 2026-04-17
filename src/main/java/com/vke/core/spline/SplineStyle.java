package com.vke.core.spline;

import com.vke.api.annotation.Warning;
import com.vke.core.color.RgbColor;

public class SplineStyle {
    public boolean filled;
    public float strokeWidth;
    public RgbColor fillColor;
    public RgbColor strokeColor;
    public JoinStyle joinStyle;
    public CapStyle capStyle;

    public SplineStyle() {
        this.filled = false;
        this.strokeWidth = 1;
        this.fillColor = null;
        this.strokeColor = RgbColor.BLACK;
        this.joinStyle = JoinStyle.Miter;
        this.capStyle = CapStyle.Flat;
    }

    public enum JoinStyle {
        Miter,
        /// WARNING: This JoinStyle is not supported rn
        Round,
    }

    public enum CapStyle {
        Flat,
        Round
    }
}
