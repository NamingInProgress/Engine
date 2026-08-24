package com.vke.core.file.jpeg.jfif.markers;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.core.file.jpeg.jfif.JfifDataMarker;
import com.vke.core.file.jpeg.jfif.Markers;
import com.vke.core.file.jpeg.scan.Frame;
import com.vke.core.file.jpeg.scan.FrameComponent;
import com.vke.core.file.jpeg.scan.FrameKind;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class SOFn extends JfifDataMarker {
    public final FrameKind kind;
    public final int precision;
    public final int Y, X;
    public final int Nf;
    public final int[] Ci;
    public final int[] Hi;
    public final int[] Vi;
    public final int[] quantTableDest;
    public final FrameComponent[] frameComponents;

    public SOFn(InputStream stream, int marker) throws IOException {
        super(stream);

        if (marker == Markers.DHP) {
            this.kind = null;
        } else {
            this.kind = IntEnum.fromInt(FrameKind.values(), marker);
        }

        this.precision = DataUtils.readU8(stream);
        this.Y = DataUtils.readU16BigEndian(stream);
        this.X = DataUtils.readU16BigEndian(stream);
        this.Nf = DataUtils.readU8(stream);
        this.Ci = new int[Nf];
        this.Hi = new int[Nf];
        this.Vi = new int[Nf];
        this.quantTableDest = new int[Nf];

        this.frameComponents = new FrameComponent[Nf];

        for (int i = 0; i < Nf; i++) {
            Ci[i] = DataUtils.readU8(stream);
            int HiVi = DataUtils.readU8(stream);
            Hi[i] = (HiVi >>> 4) & 0b1111;
            Vi[i] = HiVi & 0b1111;
            quantTableDest[i] = DataUtils.readU8(stream);
            frameComponents[i] = new FrameComponent(Ci[i], Hi[i], Vi[i], quantTableDest[i]);
        }
    }

    public Frame intoFrame() {
        return new Frame(kind, precision, X, Y, frameComponents);
    }
}
