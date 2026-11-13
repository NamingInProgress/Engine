package com.vke.core.file.deflate.decompress.block;

import com.vke.core.file.deflate.decompress.BitUtils;
import com.vke.core.file.deflate.decompress.DeflateBlock;
import com.vke.core.file.deflate.decompress.check.Checksum32;
import com.vke.core.file.io.bit.BitStreamUtils;
import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.io.bit.BitOrdering;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class UncompressedBlock implements DeflateBlock {
    private final boolean bFinal;
    private boolean initialized;

    private int length;
    private int bytesRead;

    public UncompressedBlock(boolean bFinal) {
        this.bFinal = bFinal;
    }


    @Override
    public int inflateBytes(BitInputStream inputStream, byte[] dst, int start, int length, @Nullable Checksum32 checksum) throws IOException {
        inputStream.setOrdering(BitOrdering.LSB_FIRST);
        if (!initialized) {
            inputStream.alignToByte();
            this.length = BitStreamUtils.readLittleEndian16(inputStream);
            int NLEN = BitStreamUtils.readLittleEndian16(inputStream);

            initialized = true;
        }
        try {
            if (bytesRead >= this.length) return -1;
            //we are guaranteed to be aligned to bytes here
            int read = inputStream.readBytes(dst, start, length);
            if (checksum != null) {
                //i have to find a better way
                for (int n = 0; n < read; n++) {
                    checksum.nextByte(dst[start + n]);
                }
            }
            bytesRead += read;
            return read;
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public boolean bFinal() {
        return bFinal;
    }
}
