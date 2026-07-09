package com.vke.core.file.ogg;

import com.vke.core.file.deflate.decompress.BitUtils;
import com.vke.core.file.utils.Ascii4;
import com.vke.core.file.utils.DataUtils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class OggPageReader {
    public static final Ascii4 MAGIC = new Ascii4('O', 'g', 'g', 'S');

    private final InputStream stream;

    public OggPageReader(InputStream stream) {
        this.stream = stream;
    }

    public PageHeader parsePageHeader() throws IOException {
        try {
            Ascii4 magic = Ascii4.read(stream);
            if (!magic.equals(MAGIC)) {
                throw new IOException("This stream is not an Ogg stream!");
            }
            int oggVersion = DataUtils.readU8(stream);
            int headerFlags = DataUtils.readU8(stream);
            boolean packetContinuation = BitUtils.bitsContains(headerFlags, 0x01);
            boolean bos = BitUtils.bitsContains(headerFlags, 0x02);
            boolean eos = BitUtils.bitsContains(headerFlags, 0x04);
            long granulePos = DataUtils.readU64LittleEndian(stream);
            long serialNum = DataUtils.readU32LittleEndian(stream);
            long pageSeqNum = DataUtils.readU32LittleEndian(stream);
            int crc = DataUtils.readU32LittleEndian(stream);
            int numPageSegments = DataUtils.readU8(stream);
            int[] segmentTable = DataUtils.readU8N(numPageSegments, stream);
            return new PageHeader(oggVersion, packetContinuation, bos, eos, granulePos, serialNum, pageSeqNum, crc, segmentTable);
        } catch (EOFException e) {
            return null;
        }
    }


    public record PageHeader(
            int oggVersion,
            boolean packetContinuation,
            boolean bos,
            boolean eos,
            long granulePos,
            long serialNum,
            long pageSeqNum,
            int crc,
            int[] segmentTable
    ) {}
}
