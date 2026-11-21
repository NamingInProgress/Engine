package com.vke.core.file.exr;

import com.vke.api.logger.Logger;
import com.vke.core.file.exr.decompress.ExrDecompressor;
import com.vke.core.file.exr.decompress.ExrNoneDecompressor;
import com.vke.core.file.exr.decompress.ExrZipDecompressor;
import com.vke.core.file.exr.extract.ExrBitmapExtractor;
import com.vke.core.file.exr.extract.ExrScanlineBitmapExtractor;
import com.vke.core.file.exr.extract.ExrTileBitmapExtractor;
import com.vke.core.file.exr.head.*;
import com.vke.core.file.exr.head.attr.*;
import com.vke.core.file.utils.DataUtils;
import com.vke.core.logger.LoggerFactory;
import com.vke.utils.io.LimitedInputStream;
import com.vke.utils.io.PositionedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ExrDecoder {
    public static final Logger LOGGER = LoggerFactory.get("EXR Decoder");

    private final InputStream stream;
    public final ExrHeaderHead head;
    public final ExrPart[] parts;

    private final HashMap<String, ExrAttr.Factory> attrFactories;

    private static final Map<ExrCompression, ExrDecompressor> DECOMPRESSORS = Map.of(
            ExrCompression.NO, new ExrNoneDecompressor(),
            ExrCompression.ZIP, new ExrZipDecompressor()
    );

    public ExrDecoder(InputStream stream) throws IOException {
        this.stream = stream;
        this.attrFactories = new HashMap<>();
        registerAttrs();
        this.head = new ExrHeaderHead(stream);

        if (head.isMultipart) {
            ExrMultiPartHeader headers = new ExrMultiPartHeader(this, stream);
            this.parts = new ExrPart[headers.headerCount()];
            for (int i = 0; i < headers.headerCount(); i++) {
                ExrHeader header = headers.getHeader(i);
                parts[i] = new ExrPart(header, stream);
            }
        } else {
            ExrSinglePartHeader header = new ExrSinglePartHeader(this, stream);
            this.parts = new ExrPart[] { new ExrPart(header, stream) };
        }
    }

    public final String readExrStringNull(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        int limit = head.hasLongNames ? 255 : 31;
        for (int i = 0; i < limit; i++) {
            int character = DataUtils.readU8(stream);
            if (character == 0) {
                return builder.toString();
            }
            builder.append((char) character);
        }
        throw new IOException("Could not read Exr String (expected a terminator within " + limit + " bytes, still going)");
    }

    public final ExrAttr.Factory getAttrFactory(String type) {
        var fac = attrFactories.get(type);
        if (fac == null) {
            LOGGER.debug("Type '%s' has no factory registered, skipping attribute", type);
        }
        return fac;
    }

    protected final void registerAttr(String type, ExrAttr.Factory factory) {
        this.attrFactories.put(type, factory);
    }

    public ExrChunkReader createChunkReader() {
        return new ExrChunkReader(parts, head, stream);
    }

    public ExrBitmapExtractor createBitmapExtractor(int part, boolean flipVertically) {
        //todo
        ExrPart.ImageType imageType = parts[part].getImageType(head);
        return switch (imageType) {
            case Scanline -> new ExrScanlineBitmapExtractor(this, stream, flipVertically);
            case Tile -> new ExrTileBitmapExtractor(this, stream, flipVertically);
            case DeepScan -> null;
            case DeepTile -> null;
        };
    }

    protected void registerAttrs() {
        registerAttr("int", IntAttr::new);
        registerAttr("float", FloatAttr::new);
        registerAttr("double", DoubleAttr::new);
        registerAttr("half", HalfAttr::new);
        registerAttr("box2i", Box2iAttr::new);
        registerAttr("box2f", Box2fAttr::new);
        registerAttr("bytes", BytesAttr::new);
        registerAttr("chlist", ChlistAttr::new);
        registerAttr("chromaticities", ChromaticitiesAttr::new);
        registerAttr("compression", CompressionAttr::new);
        registerAttr("envmap", EnvMapAttr::new);
        registerAttr("keycode", KeycodeAttr::new);
        registerAttr("lineOrder", LineOrderAttr::new);
        registerAttr("m33f", M33fAttr::new);
        registerAttr("m44f", M44fAttr::new);
        registerAttr("preview", PreviewAttr::new);
        registerAttr("rational", RationalAttr::new);
        registerAttr("string", StringAttr::new);
        registerAttr("stringvector", StringVectorAttr::new);
        registerAttr("tiledesc", TileDescAttr::new);
        registerAttr("timecode", TimecodeAttr::new);
        registerAttr("v2i", V2iAttr::new);
        registerAttr("v2f", V2fAttr::new);
        registerAttr("v3i", V3fAttr::new);
        registerAttr("v3f", V3fAttr::new);
    }

    public ExrDecompressor getDecompressor(ExrCompression cmpr) {
        return DECOMPRESSORS.get(cmpr);
    }

    public byte[] decompress(int length, ExrCompression cmpr, int streamLimit) throws IOException {
        byte[] dst = new byte[length];
        decompress(dst, 0, length, cmpr, streamLimit);
        return dst;
    }

    public void decompress(byte[] dst, ExrCompression cmpr, int streamLimit) throws IOException {
        decompress(dst, 0, dst.length, cmpr, streamLimit);
    }

    public long decompress(byte[] dst, int start, int length, ExrCompression cmpr, int streamLimit) throws IOException {
        ExrDecompressor decomp = getDecompressor(cmpr);
        if (decomp == null) {
            throw new IOException("No decompressor found for compression method " + cmpr);
        }
        PositionedInputStream pStream = new PositionedInputStream(stream);
        LimitedInputStream limited = new LimitedInputStream(pStream, streamLimit);
        decomp.decompress(limited, dst, start, length);
        return pStream.getPosition();
    }
}
