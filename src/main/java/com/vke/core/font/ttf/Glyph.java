package com.vke.core.font.ttf;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.ObjectArrayList;
import com.vke.core.font.ttf.table.TTFGlyfTable;
import org.joml.Matrix3f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Glyph {

    public static final Glyph EMPTY = new Glyph();

    private static final int ON_CURVE = 1 << 0;
    private static final int X_SHORT_VEC = 1 << 1;
    private static final int Y_SHORT_VEC = 1 << 2;
    private static final int REPEAT = 1 << 3;
    private static final int X_IS_SAME = 1 << 4;
    private static final int Y_IS_SAME = 1 << 5;

    private static final int ARG_1_AND_2_ARE_WORDS = 1 << 0;
    private static final int ARGS_ARE_XY_VALUES = 1 << 1;
    private static final int ROUND_XY_TO_GRID = 1 << 2;
    private static final int WE_HAVE_A_SCALE = 1 << 3;
    private static final int MORE_COMPONENTS = 1 << 5;
    private static final int WE_HAVE_AN_X_AND_Y_SCALE = 1 << 6;
    private static final int WE_HAVE_A_TWO_BY_TWO = 1 << 7;
    private static final int WE_HAVE_INSTRUCTIONS = 1 << 8;
    private static final int USE_MY_METRICS = 1 << 9;
    private static final int OVERLAP_COMPOUND = 1 << 10;

    private final TTFGlyfTable glyf;

    public final short numContours;
    public final short xMin, yMin, xMax, yMax;
    public final GlyphType type;

    public int[] endPointsOfContours;
    public int[] instructions;

    public GlyphPoint[] points;

    private Glyph() {
        this.glyf = null;
        this.numContours = 0;
        this.xMin = 0;
        this.yMin = 0;
        this.xMax = 0;
        this.yMax = 0;
        this.type = GlyphType.SIMPLE;
        this.endPointsOfContours = new int[]{0};
        this.instructions = new int[0];
        this.points = new GlyphPoint[0];
    }

    public Glyph(TTFGlyfTable glyf, TTFReader reader, long totalOffset) {
        this.glyf = glyf;
        reader.position(totalOffset);
        this.numContours = reader.i16();
        this.xMin = reader.fword();
        this.yMin = reader.fword();
        this.xMax = reader.fword();
        this.yMax = reader.fword();

        if (numContours < 0) {
            type = GlyphType.COMPOUND;
            parseCompound(reader);
        } else {
            type = GlyphType.SIMPLE;
            parseSimple(reader);
        }
    }

    private void parseCompound(TTFReader reader) {
        ObjectArrayList<GlyphPoint> points = new ObjectArrayList<>();
        IntArrayList endPoints = new IntArrayList();

        int flags;
        int offset = 0;
        do {
            flags = reader.u16();
            int glyphIndex = reader.u16();

            int arg1, arg2;

            if ((flags & ARGS_ARE_XY_VALUES) != 0) {
                if ((flags & ARG_1_AND_2_ARE_WORDS) != 0) {
                    arg1 = reader.i16();
                    arg2 = reader.i16();
                } else {
                    arg1 = reader.i8();
                    arg2 = reader.i8();
                }
            } else {
                if ((flags & ARG_1_AND_2_ARE_WORDS) != 0) {
                    arg1 = reader.u16();
                    arg2 = reader.u16();
                } else {
                    arg1 = reader.u8();
                    arg2 = reader.u8();
                }
            }

            float scale, sx, sy, m00, m01, m10, m11;
            Matrix3f transform = new Matrix3f();

            if ((flags & ARGS_ARE_XY_VALUES) != 0) {
                transform.m20(arg1);
                transform.m21(arg2);
            }

            if ((flags & WE_HAVE_A_SCALE) != 0) {
                scale = (float) reader.f2Dot14();
                transform.m00(scale);
                transform.m11(scale);
            } else if ((flags & WE_HAVE_AN_X_AND_Y_SCALE) != 0) {
                sx = (float) reader.f2Dot14();
                sy = (float) reader.f2Dot14();
                transform.m00(sx);
                transform.m11(sy);
            } else if ((flags & WE_HAVE_A_TWO_BY_TWO) != 0) {
                m00 = (float) reader.f2Dot14();
                m01 = (float) reader.f2Dot14();
                m10 = (float) reader.f2Dot14();
                m11 = (float) reader.f2Dot14();
                transform.m00(m00);
                transform.m10(m01);
                transform.m01(m10);
                transform.m11(m11);
            }

            int pos = reader.position();

            Glyph component = glyf.getOrRead(glyphIndex);

            reader.position(pos);

            for (GlyphPoint p : component.points) {
                Vector3f tmp = new Vector3f(p.vec.x, p.vec.y, 1);
                tmp.mul(transform);
                points.add(new GlyphPoint(new Vector2f(tmp.x, tmp.y), p.onCurve));
            }

            for (int endPointOfContour : component.endPointsOfContours) {
                endPoints.add(endPointOfContour + offset);
            }
            offset += component.endPointsOfContours[component.endPointsOfContours.length - 1] + 1;
        } while ((flags & MORE_COMPONENTS) != 0);

        this.points = points.toArray(GlyphPoint.class);
        this.endPointsOfContours = endPoints.toArray();

        if ((flags & WE_HAVE_INSTRUCTIONS) != 0) {
            int count = reader.u16();
            this.instructions = new int[count];
            for (int i = 0; i < count; i++) {
                this.instructions[i] = reader.u8();
            }
        } else {
            this.instructions = new int[0];
        }
    }

    private void parseSimple(TTFReader reader) {
        endPointsOfContours = new int[numContours];

        for (short i = 0; i < numContours; i++) {
            endPointsOfContours[i] = reader.u16();
        }

        int numPoints = endPointsOfContours[numContours - 1] + 1;

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
        Vector2f[] tempPoints = new Vector2f[numPoints];
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

            tempPoints[i] = new Vector2f(xPositions[i], y);
        }

        ObjectArrayList<GlyphPoint> modified = new ObjectArrayList<>();

        int contourStart = 0;
        int[] newEndPoints = new int[endPointsOfContours.length];
        int emitted = 0;

        for (int c = 0; c < endPointsOfContours.length; c++) {
            int contourEnd = endPointsOfContours[c];

            for (int i = contourStart; i <= contourEnd; i++) {
                modified.add(new GlyphPoint(tempPoints[i], (flags[i] & ON_CURVE) != 0));
                emitted++;

                int next = (i == contourEnd) ? contourStart : i + 1;

                if ((flags[i] & ON_CURVE) == 0 &&
                        (flags[next] & ON_CURVE) == 0) {

                    Vector2f a = tempPoints[i];
                    Vector2f b = tempPoints[next];

                    modified.add(new GlyphPoint(new Vector2f(
                            (a.x + b.x) * 0.5f,
                            (a.y + b.y) * 0.5f
                    ), true));
                    emitted++;
                }
            }

            newEndPoints[c] = emitted - 1;
            contourStart = contourEnd + 1;
        }

        this.points = modified.toArray(GlyphPoint.class);
        this.endPointsOfContours = newEndPoints;
    }

    public enum GlyphType {
        SIMPLE,
        COMPOUND
    }

}
