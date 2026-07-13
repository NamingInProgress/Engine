package com.vke.core.file.ogg.vorbis.data.floor;

import com.vke.core.file.ogg.vorbis.Helpers;
import com.vke.core.file.ogg.vorbis.data.window.WindowData;

import java.io.IOException;

public class Floor1Data {
    public static final int[] RANGE_VALS = {256, 128, 86, 64};

    public final boolean unused;
    private int[] xList, finalY;
    private boolean[] step2Flag;
    private int multiplier, floorValues;

    public Floor1Data(boolean unused) {
        this.unused = unused;
    }

    public Floor1Data(boolean unused, int[] xList, int[] finalY, boolean[] step2Flag, int multiplier, int floorValues) {
        this.unused = unused;
        this.xList = xList;
        this.finalY = finalY;
        this.step2Flag = step2Flag;
        this.multiplier = multiplier;
        this.floorValues = floorValues;
    }

    public float[] compute(WindowData windowData) throws IOException {
        sortArrays();
        int n = windowData.n;

        float[] floor = new float[n];

        int hx = 0;
        int hy = 0;
        int lx = 0;
        int ly = finalY[0] * multiplier;
        for (int i = 1; i < floorValues; i++) {
            if (step2Flag[i]) {
                hy = finalY[i] * multiplier;
                hx = xList[i] * multiplier;
                Helpers.render_line(lx, ly, hx, hy, floor);
                lx = hx;
                ly = hy;
            }
        }

        if (hx < n) {
            Helpers.render_line(hx, hy, n, hy, floor);
        }

        if (hx > n) {
            //idk what to do here tbh
            //https://xiph.org/vorbis/doc/Vorbis_I_spec.pdf
            //step 2: curve synthesis -> pt. 14
        }

        for (int i = 0; i < floor.length; i++) {
            floor[i] = Floor1InverseDbTable.VALUES[(int) floor[i]];
        }

        return floor;
    }

    private void sortArrays() {
        quicksortStep(0, xList.length);
    }

    //ascending order
    private void quicksortStep(int start, int end) {
        if (end - start <= 1)
            return;

        int pivot = xList[start + (end - start) / 2];

        int i = start;
        int j = end - 1;

        while (i <= j) {
            while (xList[i] < pivot) i++;
            while (xList[j] > pivot) j--;

            if (i <= j) {
                swap(i, j);
                i++;
                j--;
            }
        }

        quicksortStep(start, j + 1);
        quicksortStep(i, end);
    }

    private void swap(int a, int b) {
        if (a == b) return;

        int t1 = xList[a];
        xList[a] = xList[b];
        xList[b] = t1;

        t1 = finalY[a];
        finalY[a] = finalY[b];
        finalY[b] = t1;

        boolean t2 = step2Flag[a];
        step2Flag[a] = step2Flag[b];
        step2Flag[b] = t2;
    }
}
