package com.vke.core.font.ttf;

import org.joml.Vector2f;

public class Glyph {

    private static final int ON_CURVE = 1 << 0;
    private static final int X_SHORT_VEC = 1 << 1;
    private static final int Y_SHORT_VEC = 1 << 2;
    private static final int REPEAT = 1 << 3;
    private static final int X_IS_SAME = 1 << 4;
    private static final int Y_IS_SAME = 1 << 5;

    public final short numContours;
    public final short xMin, yMin, xMax, yMax;
    public final GlyphType type;

    public int[] endPointsOfContours;
    public int[] instructions;

    public int numPoints;

    public Vector2f[] points;

    public Glyph(TTFReader reader) {
        this.numContours = reader.i16();
        this.xMin = reader.fword();
        this.yMin = reader.fword();
        this.xMax = reader.fword();
        this.yMax = reader.fword();

        if (numContours < 0) {
            type = GlyphType.COMPOUND;
        } else {
            type = GlyphType.SIMPLE;
            parseSimple(reader);
        }

        insertPoints();
    }

    private void insertPoints() {

    }

    private void parseSimple(TTFReader reader) {
        endPointsOfContours = new int[numContours];

        for (short i = 0; i < numContours; i++) {
            endPointsOfContours[i] = reader.u16();
        }

        numPoints = endPointsOfContours[numContours - 1] + 1;

        int instructionCount = reader.u16();
        instructions = new int[instructionCount];
        for (int i = 0; i < instructionCount; i++) {
            instructions[i] = reader.u8();
        }

        int[] flags = new int[numPoints];
        int flagsCount = 0;

        while (flagsCount < numPoints) {
            int flag = reader.u8();
            flags[flagsCount++] = flag;

            if ((flag & REPEAT) != 0) {
                int repeat = reader.u8();
                for (int i = 0; i < repeat; i++) {
                    flags[flagsCount++] = flag;
                }
            }
        }

        int[] xPositions = new int[numPoints];
        this.points = new Vector2f[numPoints];
        int x = 0, y = 0;

        for (int i = 0; i < numPoints; i++) {
            int flag = flags[i];
            boolean shortVec = (flag & X_SHORT_VEC) != 0;
            boolean sameX = (flag & X_IS_SAME) != 0;

            if (shortVec) {
                int dx = reader.u8();
                if (sameX) {
                    x += dx;
                } else {
                    x -= dx;
                }
            } else {
                if (!sameX) {
                    x += reader.i16();
                }
                // else delta = 0 so we don't read anything
            }

            xPositions[i] = x;
        }

        for (int i = 0; i < numPoints; i++) {
            int flag = flags[i];
            boolean shortVec = (flag & Y_SHORT_VEC) != 0;
            boolean sameY = (flag & Y_IS_SAME) != 0;

            if (shortVec) {
                int dy = reader.u8();
                if (sameY) {
                    y += dy;
                } else {
                    y -= dy;
                }
            } else {
                if (!sameY) {
                    y += reader.i16();
                }
                // else delta = 0 so we don't read anything
            }

            points[i] = new Vector2f(xPositions[i], y);
        }
    }

    public enum GlyphType {
        SIMPLE,
        COMPOUND
    }

}
