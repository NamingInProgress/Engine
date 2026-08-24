package com.vke.core.file.jpeg.jfif.markers;

import com.vke.core.file.jpeg.idct.Const;
import com.vke.core.file.jpeg.jfif.JfifDataMarker;
import com.vke.core.file.utils.BitPackerUtils;
import com.vke.core.file.utils.DataUtils;
import com.vke.utils.io.PositionedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class DQT extends JfifDataMarker {
    public final Table[] tables;

    public DQT(InputStream stream) throws IOException {
        super(stream);

        int remaining = size - 2;

        ArrayList<Table> tables = new ArrayList<>();
        while (remaining > 0) {
            PositionedInputStream tableStream = new PositionedInputStream(stream);
            Table table = new Table(tableStream);
            tables.add(table);
            remaining -= (int) tableStream.getPosition();
        }

        this.tables = tables.toArray(Table[]::new);
    }

    public static class Table {
        public final int precision, destination;
        public final int[] coefficients;

        public Table(InputStream stream) throws IOException {
            int[] PqTq = BitPackerUtils.unpackU4BE(DataUtils.readU8(stream), 2);
            this.precision = PqTq[0];
            this.destination = PqTq[1];

            if (destination < 0 || destination > 3) {
                throw new IOException("Destination has to be between 0 and =3!");
            }

            this.coefficients = new int[64];
            for (int i = 0; i < 64; i++) {
                int val;
                if (precision == 0) {
                    val = DataUtils.readU8(stream);
                } else if (precision == 1) {
                    val = DataUtils.readU16BigEndian(stream);
                } else {
                    throw new IOException("Invalid quantization table precision: " + precision);
                }

                coefficients[Const.QUANT_ORDER[i]] = val;
            }
        }
    }
}
