package com.vke.core.font;

import com.vke.core.font.ttf.Glyph;
import com.vke.core.font.ttf.GlyphPoint;
import org.joml.Vector2f;

public class Tessellator {

    public static void accept(Glyph g) {
        int contourStartIndex = 0;
        int[] endPointsOfContours = g.endPointsOfContours;
        for (int i = 0; i < endPointsOfContours.length; i++) {
            float area = 0;

            int contourEndIndex = endPointsOfContours[i];
            int numPoints = contourEndIndex - contourStartIndex + 1;

            for (int j = contourStartIndex; j < contourStartIndex + numPoints; j++) {
                int next = (j == contourEndIndex) ? contourStartIndex : j + 1;
                GlyphPoint a = g.points[j];
                GlyphPoint b = g.points[next];

                area += a.vec.x * b.vec.y - b.vec.x * a.vec.y;
            }

            if (area < 0) {
                // Flip
            }

            contourStartIndex = contourEndIndex + 1;
        }

    }

}
