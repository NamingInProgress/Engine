package com.vke.core.file.jpeg.jfif.markers;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.core.file.jpeg.ArithmeticTable;
import com.vke.core.file.jpeg.jfif.JfifDataMarker;
import com.vke.core.file.jpeg.jfif.TableClass;
import com.vke.core.file.utils.BitPackerUtils;
import com.vke.core.file.utils.DataUtils;
import com.vke.utils.io.PositionedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class DAC extends JfifDataMarker {
    public final ArithmeticTable[] arithmeticTables;

    public DAC(InputStream stream) throws IOException {
        super(stream);

        ArrayList<ArithmeticTable> tables = new ArrayList<>();
        int remaining = this.size - 2;
        while (remaining > 0) {
            PositionedInputStream tableStream = new PositionedInputStream(stream);

            int[] TcTb = BitPackerUtils.unpackU4BE(DataUtils.readU8(tableStream), 2);
            TableClass tableClass = IntEnum.fromInt(TableClass.values(), TcTb[0]);
            int tableDest = TcTb[1];
            int conditioning = DataUtils.readU8(tableStream);

            tables.add(new ArithmeticTable(tableDest, tableClass, conditioning));

            remaining -= (int) tableStream.getPosition();
        }

        this.arithmeticTables = tables.toArray(new ArithmeticTable[0]);
    }
}
