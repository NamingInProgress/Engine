package com.vke.core.file.spirv;

import com.vke.core.file.utils.DataUtils;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class SpirvDecoder {
    private final InputStream stream;
    private boolean headerParsed;

    public SpirvDecoder(InputStream stream) {
        this.stream = stream;
    }

    private int word() throws IOException {
        DataUtils.enableThrowOnEOF();
        int w = DataUtils.readU32LittleEndian(stream);
        DataUtils.popConfig();
        return w;
    }

    private SpirvInstruction nextInstruction() throws IOException {
        if (!headerParsed) {
            for (int i = 0; i < 5; i++) {
                word();
            }

            headerParsed = true;
        }

        int firstWord = word();
        int wordCount = (firstWord >> 16) & 0xFFFF;
        int opcode = firstWord & 0xFFFF;
        int opAmt = wordCount - 1;

        int[] operands = new int[opAmt];
        for (int i = 0; i < opAmt; i++) {
            operands[i] = word();
        }
        return new SpirvInstruction(opcode, operands);
    }

    public Iter<SpirvInstruction> instructions() {
        return this.new I();
    }

    private class I implements Iter<SpirvInstruction> {
        @Override
        public @NotNull Option<SpirvInstruction> next() {
            return Option.useIfNotFaulty(SpirvDecoder.this::nextInstruction);
        }
    }
}