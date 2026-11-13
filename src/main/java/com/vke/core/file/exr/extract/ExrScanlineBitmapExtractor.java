package com.vke.core.file.exr.extract;

import com.vke.core.VKEngine;
import com.vke.core.color.RgbColor;
import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.head.ExrHeader;
import com.vke.core.file.exr.head.attr.Box2iAttr;
import com.vke.core.file.exr.out.ExrBitmap;
import com.vke.core.file.utils.DataUtils;
import com.vke.core.image.Bitmap;
import com.vke.core.image.LdrBitmap;
import com.vke.core.profiler.DummyProfilerImpl;
import com.vke.core.profiler.ProfilerPrinter;
import com.vke.core.profiler.service.Profiler;
import com.vke.core.profiler.service.ProfilerImpl;
import com.vke.core.rendering.vulkan.extent.VulkanExtentUtils;
import com.vke.core.rendering.vulkan.swapchain.SwapchainUtils;
import com.vke.utils.Stopwatch;
import com.vke.utils.console.AnsiColors;
import com.vke.utils.console.PrettyTable;

import java.io.IOException;
import java.io.InputStream;

public class ExrScanlineBitmapExtractor extends ExrScanlineExtractor {
    private final int targetPart;
    private final ExrHeader header;

    public ExrScanlineBitmapExtractor(ExrDecoder decoder, InputStream stream, int targetPart) {
        super(decoder, stream);
        this.targetPart = targetPart;
        this.header = decoder.parts[targetPart].header();
    }

    public Bitmap readIntoBitmap(boolean flipVertically, VKEngine engine) throws IOException {
        Box2iAttr dataWindow = header.dataWindow;
        Box2iAttr dispWindow = header.displayWindow;
        ExrBitmap bitmap = new ExrBitmap(header.chlist, dataWindow.width(), dataWindow.height(), flipVertically);
        if (dataWindow.needsBg(dispWindow)) {
            //todo
            //bitmap.fill(RgbColor.VKE);
        }

        int w = dataWindow.width();
        int h = dataWindow.height();

        System.out.println("w: " + w + ",h: " + h);

        Stopwatch readWatch = new Stopwatch();
        Stopwatch writeWatch = new Stopwatch();
        Stopwatch totalWatch = new Stopwatch();

        totalWatch.start();
        more = true;
        while (more) {
            readWatch.start();
            readScanlines();
            readWatch.stop();
            if (part != targetPart) {
                continue;
            }

            //todo: handle x and y sampling
            writeWatch.start();
            int scanlineSize = w * header.computePixelStride();
            for (int scan = 0; scan < blockScans; scan++) {
                int scanY = yPos + scan;
                bitmap.setScanline(scanY, samples, scan * scanlineSize);
            }
            writeWatch.stop();
        }
        totalWatch.stop();

        PrettyTable table = new PrettyTable();
        table.column("operation");
        table.column("time (ms)");
        table.newRow();
        table.column("read scans");
        table.column(readWatch.getMillis());
        table.newRow();
        table.column("write scans");
        table.column(writeWatch.getMillis());
        table.newRow();
        table.column("total");
        table.column(totalWatch.getMillis());


        System.out.println(table);

        return bitmap;
    }
}
