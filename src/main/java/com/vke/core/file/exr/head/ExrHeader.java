package com.vke.core.file.exr.head;

import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.head.attr.*;
import com.vke.core.file.utils.DataUtils;
import com.vke.utils.IntUtils;
import com.vke.utils.io.BytesInFrontOfInputStream;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class ExrHeader {
    private final ExrDecoder decoder;

    public final ExrAttrMap attrs;

    public final ChlistAttr chlist;
    public final CompressionAttr compression;
    public final Box2iAttr dataWindow, displayWindow;
    public final LineOrderAttr lineOrder;
    public final FloatAttr pixelAspectRatio;
    public final V2fAttr screenWindowCenter;
    public final FloatAttr screenWindowWidth;

    public final @Nullable TileDescAttr tiles;

    public ExrHeader(ExrDecoder dec, InputStream stream) throws IOException {
        this.decoder = dec;
        this.attrs = new ExrAttrMap();

        int next;
        while ((next = DataUtils.readU8(stream)) > 0) {
            InputStream attrStream = new BytesInFrontOfInputStream(stream, (byte) next);
            ExrAttributeField field = new ExrAttributeField(dec, attrStream);
            this.attrs.put(field.name, field.value);
        }

        this.chlist = expectAttr(attrs, "channels");
        this.compression = expectAttr(attrs, "compression");
        this.dataWindow = expectAttr(attrs, "dataWindow");
        this.displayWindow = expectAttr(attrs, "displayWindow");
        this.lineOrder = expectAttr(attrs, "lineOrder");
        this.pixelAspectRatio = expectAttr(attrs, "pixelAspectRatio");
        this.screenWindowCenter = expectAttr(attrs, "screenWindowCenter");
        this.screenWindowWidth = expectAttr(attrs, "screenWindowWidth");

        ExrAttr tiles = attrs.get("tiles");
        if (tiles != null) {
            this.tiles = (TileDescAttr) tiles;
        } else {
            this.tiles = null;
        }
    }

    @SuppressWarnings("unchecked unchecked unchedeck")
    protected static <A extends ExrAttr> A expectAttr(ExrAttrMap attrs, String name) throws IOException {
        ExrAttr attr = attrs.get(name);
        if (attr == null) {
            throw new IOException(String.format("Hilarious! This exr file doesnt even have a '%s' inside the header! Ha!", name));
        }
        return (A) attr;
    }

    public int computeOffsetCount() throws IOException {
        ExrHeaderHead head = decoder.head;
        IntAttr attr = (IntAttr) attrs.get("chunkCount");
        if (!head.isMultipart) {
            //case 1
            if (attr != null) {
                return attr.value;
            } else {
                int w = dataWindow.width();
                //FUCKKK I DONT WANNA DO THIS
                if (head.isSingleTile) {
                    //tiles
                    if (tiles == null) {
                        throw new IOException("For tile images, the attribute 'tiles' must be available!");
                    }

                    int h = dataWindow.height();

                    ExrLevelMode lvlm = tiles.levelMode;
                    ExrRoundingMode rm = tiles.roundingMode;
                    int count = 0;
                    switch (lvlm) {
                        case ONE_LEVEL -> {
                            int x = IntUtils.fastCeilDivPositive(w, tiles.xSize);
                            int y = IntUtils.fastCeilDivPositive(h, tiles.ySize);
                            count += x * y;
                        }
                        case MIPMAP_LEVELS -> {
                            for (int l = 0; l < lvlm.numberOfLevelsX(w, h, rm); l++) {
                                int lw = ExrLevelMode.levelSize(w, l, rm);
                                int lh = ExrLevelMode.levelSize(h, l, rm);
                                int x = IntUtils.fastCeilDivPositive(lw, tiles.xSize);
                                int y = IntUtils.fastCeilDivPositive(lh, tiles.ySize);
                                count += x * y;
                            }
                        }
                        case RIPMAP_LEVELS -> {
                            for (int lx = 0; lx < lvlm.numberOfLevelsX(w, h, rm); lx++) {
                                for (int ly = 0; ly < lvlm.numberOfLevelsY(w, h, rm); ly++) {
                                    int lw = ExrLevelMode.levelSize(w, lx, rm);
                                    int lh = ExrLevelMode.levelSize(h, ly, rm);
                                    int x = IntUtils.fastCeilDivPositive(lw, tiles.xSize);
                                    int y = IntUtils.fastCeilDivPositive(lh, tiles.ySize);
                                    count += x * y;
                                }
                            }
                        }
                    }
                    return count;
                } else {
                    //scanlines
                    int scanlines = compression.compression.numberOfScanlinesPerBlock();
                    return IntUtils.fastCeilDivPositive(w, scanlines);
                }
            }
        } else {
            //case 2
            if (attr == null) {
                throw new IOException("When file is multipart, the 'chunkCount' attribute must be present! And guess what, it is NOT RIGHT NOW GET OUUTT!!");
            }

            return attr.value;
        }
    }

    public int computeScanBlockByteSize(int yPos, int height) {
        int size = 0;
        int w = dataWindow.width();
        for (ExrChannel channel : chlist.channels.values()) {
            int ySamp = channel.ySampling();
            int facy = height;
            if (ySamp != 1) {
                facy = 0;
                for (int y = 0; y < height; y++) {
                    if ((y + yPos) % ySamp == 0) {
                        facy++;
                    }
                }
            }

            ExrPixelType pxType = channel.pixelType();
            int facx = IntUtils.fastCeilDivPositive(w, channel.xSampling());
            size += pxType.byteSize * facx * facy;
        }
        return size;
    }
}
