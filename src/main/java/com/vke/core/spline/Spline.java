package com.vke.core.spline;

import com.vke.utils.iter.Iter;

import java.util.ArrayList;

public class Spline {
    private final ArrayList<SplineCommand> commands;

    public Spline() {
        this.commands = new ArrayList<>();
    }

    public Spline moveTo(float x, float y) {
        this.commands.add(new SplineCommand(SplineCommand.Type.MoveTo, new float[] {x, y}));
        return this;
    }

    public Spline lineTo(float x, float y) {
        this.commands.add(new SplineCommand(SplineCommand.Type.LineTo, new float[] {x, y}));
        return this;
    }

    public Spline quadTo(float x, float y, float cx, float cy) {
        this.commands.add(new SplineCommand(SplineCommand.Type.QuadTo, new float[] {x, y, cx, cy}));
        return this;
    }

    public Spline CubicTo(float x, float y, float c1x, float c1y, float c2x, float c2y) {
        this.commands.add(new SplineCommand(SplineCommand.Type.CubicTo, new float[] {x, y, c1x, c1y, c2x, c2y}));
        return this;
    }

    public Spline close() {
        this.commands.add(new SplineCommand(SplineCommand.Type.Close, new float[0]));
        return this;
    }

    public Iter<SplineCommand> commands() {
        return Iter.of(commands);
    }

    public FlattenedCurve flatten(float tolerance) {
        float currentX = 0, currentY = 0;
        //educated array size guess
        ArrayList<FlatPoint> points = new ArrayList<>();
        outer:
        for (SplineCommand command : commands) {
            switch (command.getType()) {
                case MoveTo -> {
                    if (!points.isEmpty()) {
                        points.add(new FlatPoint(currentX, currentY, true));
                    }

                    currentX = command.getData()[0];
                    currentY = command.getData()[1];
                    points.add(new FlatPoint(currentX, currentY, false));
                }
                case LineTo -> {
                    currentX = command.getData()[0];
                    currentY = command.getData()[1];
                    points.add(new FlatPoint(currentX, currentY, false));
                }
                case QuadTo -> {
                    float[] data = command.getData();
                    float tox = data[0]; float toy = data[1];
                    float cx = data[2]; float cy = data[3];

                    Bezier2 bezier = new Bezier2(currentX, currentY, cx, cy, tox, toy);
                    quadRecursive(bezier, points, tolerance);

                    currentX = tox;
                    currentY = toy;
                }
                case CubicTo -> {
                    float[] data = command.getData();
                    float tox = data[0]; float toy = data[1];
                    float cx1 = data[2]; float cy1 = data[3];
                    float cx2 = data[4]; float cy2 = data[5];

                    Bezier3 bezier = new Bezier3(currentX, currentY, cx1, cy1, cx2, cy2, tox, toy);
                    cubicRecursive(bezier, points, tolerance);

                    currentX = tox;
                    currentY = toy;
                }
                case Close -> {
                    break outer;
                }
            }
        }
        return new FlattenedCurve(points.toArray(FlatPoint[]::new));
    }

    private void quadRecursive(Bezier2 bezier, ArrayList<FlatPoint> out, float tolerance) {
        if (bezier.isBasicallyALine(tolerance)) {
            float[] endPoint = bezier.endPoint();
            out.add(new FlatPoint(endPoint[0], endPoint[1], false));
        } else {
            Bezier2[] split = bezier.split(0.5f);
            quadRecursive(split[0], out, tolerance);
            quadRecursive(split[1], out, tolerance);
        }
    }

    private void cubicRecursive(Bezier3 bezier, ArrayList<FlatPoint> out, float tolerance) {
        if (bezier.isBasicallyALine(tolerance)) {
            float[] endPoint = bezier.endPoint();
            out.add(new FlatPoint(endPoint[0], endPoint[1], false));
        } else {
            Bezier3[] split = bezier.split(0.5f);
            cubicRecursive(split[0], out, tolerance);
            cubicRecursive(split[1], out, tolerance);
        }
    }
}
