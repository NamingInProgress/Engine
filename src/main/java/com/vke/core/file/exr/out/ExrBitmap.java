package com.vke.core.file.exr.out;

import com.carrotsearch.hppc.IntArrayList;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.core.file.exr.head.ExrChannel;
import com.vke.core.file.exr.head.ExrPixelType;
import com.vke.core.file.exr.head.attr.ChlistAttr;
import com.vke.core.file.utils.BitPackerUtils;
import com.vke.core.file.utils.DataUtils;
import com.vke.core.image.Bitmap;
import com.vke.utils.Utils;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.PriorityQueue;

public class ExrBitmap implements Bitmap {
    private final Format format;
    private boolean hasAlpha;
    private final int width, height;
    private final boolean flipVertically;

    private final byte[] rgb_a_bytes;

    private final ChType[] chOrder;
    private final int nch;
    private int nch_total;
    private final ExrPixelType[] pxTypes;
    private final int[] pxSizes;
    private ExrPixelType targetPxType;

    private final byte[] chBuf;

    public ExrBitmap(ChlistAttr chlist, int width, int height, boolean flipVertically) {
        this.width = width;
        this.height = height;
        this.flipVertically = flipVertically;

        PriorityQueue<UsedChannel> channels = new PriorityQueue<>(Comparator.comparing(uc -> uc.channel().name()));
        this.nch_total = 0;
        IntArrayList chPxSizes = new IntArrayList();
        for (ExrChannel channel : chlist.channels.values()) {
            try {
                ChType type = ChType.valueOf(channel.name());
                if (type == ChType.A) {
                    hasAlpha = true;
                }
                channels.add(new UsedChannel(channel, nch_total));
            } catch (IllegalArgumentException ignore) {}
            nch_total++;
            chPxSizes.add(channel.pixelType().byteSize);
        }

        this.pxSizes = chPxSizes.toArray();

        this.chOrder = new ChType[nch_total];
        this.pxTypes = new ExrPixelType[nch_total];

        this.nch = chOrder.length;

        this.targetPxType = ExrPixelType.UINT;
        int i = 0;
        while (!channels.isEmpty()) {
            UsedChannel usedChannel = channels.poll();
            ExrChannel channel = usedChannel.channel;
            ChType type = ChType.valueOf(channel.name());
            this.chOrder[i] = type;
            ExrPixelType pxType = channel.pixelType();
            this.pxTypes[i] = pxType;
            this.targetPxType = pxType.bigger(targetPxType);
            i++;
        }

        //FUCK YOU VULKAN YOU WASTED SO MUCH TIME OF MY FUCKING LIFE FUCK YOU FUCK YOU FUCK YOU
        hasAlpha = Utils.TRUE;

        if (hasAlpha) {
            this.format = switch (targetPxType) {
                case UINT -> Format.RGBA32UI;
                case HALF -> Format.RGBA16F;
                case FLOAT -> Format.RGBA32F;
            };
        } else {
            this.format = switch (targetPxType) {
                case UINT -> Format.RGB32UI;
                case HALF -> Format.RGB16F;
                case FLOAT -> Format.RGB32F;
            };
        }

        this.rgb_a_bytes = new byte[pixelStride() * width * height];
        this.chBuf = new byte[targetPxType.byteSize];
    }

    private record UsedChannel(ExrChannel channel, int global_idx) {}

    public void setScanline(int y, byte[] scanline, int start) {
        setShortScanline(0, y, scanline, start, width);
    }

    public void setShortScanline(int x, int y, byte[] scanline, int start, int width) {
        if (y < 0) return;

        y = flipVertically ? (height - 1 - y) : y;
        if (y < 0 || y >= height) return;

        int pixel_i = y * width * pixelStride();
        int byte_i = start;
        boolean alphaFound = false;
        for (int ch = 0; ch < nch_total; ch++) {
            ChType chtype = chOrder[ch];
            if (chtype == null) {
                byte_i += pxSizes[ch] * width;
                continue;
            }
            if (chtype == ChType.A) {
                alphaFound = true;
            }
            int chOffset = chtype.ordinal() * targetPxType.byteSize;
            ExrPixelType pxtype = pxTypes[ch];
            int pixel_i_inner = pixel_i + chOffset + x * pixelStride();
            for (int xRel = 0; xRel < width; xRel++) {
                toTarget(scanline, byte_i, pxtype);
                System.arraycopy(chBuf, 0, rgb_a_bytes, pixel_i_inner, targetPxType.byteSize);
                byte_i += pxtype.byteSize;
                pixel_i_inner += pixelStride();
            }
        }
        if (!alphaFound) {
            int chOffset = ChType.A.ordinal() * targetPxType.byteSize;
            int pixel_i_inner = pixel_i + chOffset + x * pixelStride();
            for (int xRel = 0; xRel < width; xRel++) {
                //default alpha value
                switch (targetPxType) {
                    case UINT -> {
                        BitPackerUtils.packU32LE(255, chBuf, 0);
                    }
                    case HALF -> {
                        short bits = Float.floatToFloat16(1.0f);
                        BitPackerUtils.packU16LE(bits, chBuf, 0);
                    }
                    case FLOAT -> {
                        int bits = Float.floatToIntBits(1.0f);
                        BitPackerUtils.packU32LE(bits, chBuf, 0);
                    }
                }
                System.arraycopy(chBuf, 0, rgb_a_bytes, pixel_i_inner, targetPxType.byteSize);
                pixel_i_inner += pixelStride();
            }
        }
    }

    private void toTarget(byte[] bytes, int start, ExrPixelType pxtype) {
        if (pxtype == targetPxType) {
            System.arraycopy(bytes, start, chBuf, 0, targetPxType.byteSize);
        } else {
            switch (pxtype) {
                case UINT -> {
                    int value = uint(bytes, start);
                    if (targetPxType == ExrPixelType.HALF) {
                        short bits = Float.floatToFloat16((float) value);
                        BitPackerUtils.packU16LE(bits, chBuf, 0);
                    } else {
                        //float
                        int bits = Float.floatToIntBits((float) value);
                        BitPackerUtils.packU32LE(bits, chBuf, 0);
                    }
                }
                case HALF -> {
                    if (targetPxType == ExrPixelType.FLOAT) {
                        short bits = (short) ((bytes[start]) | ((bytes[start + 1]) << 8));
                        float f = Float.float16ToFloat(bits);
                        int newBits = Float.floatToIntBits(f);
                        BitPackerUtils.packU32LE(newBits, chBuf, 0);
                    }
                    //all other cases are ruled out from the whole system of this class
                }
                case FLOAT -> {
                    //to if stmt does this case too
                }
            }
        }
    }

    private static int uint(byte[] arr, int start) {
        return DataUtils.dcba(arr[start], arr[start + 1], arr[start + 2], arr[start + 3]);
    }

    @Override
    public Format dataFormat() {
        return format;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public boolean hasAlphaChannel() {
        return hasAlpha;
    }

    @Override
    public int pixelStride() {
        int n = hasAlpha ? 4 : 3;
        return targetPxType.byteSize * n;
    }

    @Override
    public void uploadIntoBytebuffer(ByteBuffer buffer) {
        buffer.put(rgb_a_bytes);
    }

    private enum ChType {
        R, G, B, A
    }
}
