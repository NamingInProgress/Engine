package com.vke.core.rendering;

import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;

public class SmartMatrixUtils {
    public static void writePerspectiveMatrix(BufferSlice writer, float nr, float nt, float fnfn, float f2nfn) {
        writePerspectiveMatrix(writer, nr, nt, fnfn, f2nfn, true);
    }

    public static void writePerspectiveMatrix(BufferSlice writer, float nr, float nt, float fnfn, float f2nfn, boolean rowMajor) {
        if (rowMajor) {
            writer.float4(nr, 0, 0, 0);
            writer.float4(0, nt, 0, 0);
            writer.float4(0, 0, fnfn, f2nfn);
            writer.float4(0, 0, -1, 0);
        } else {
            writer.float4(nr, 0, 0, 0);
            writer.float4(0, nt, 0, 0);
            writer.float4(0, 0, fnfn, -1);
            writer.float4(0, 0, f2nfn, 0);
        }
    }

    public static void writeOrthographicMatrix(BufferSlice writer, float torml, float totmb, float mtofmn, float mfpnofmn, float mtpbotmb, float mrplorml) {
        writeOrthographicMatrix(writer, torml, totmb, mtofmn, mfpnofmn, mtpbotmb, mrplorml, true);
    }

    public static void writeOrthographicMatrix(BufferSlice writer, float torml, float totmb, float mtofmn, float mfpnofmn, float mtpbotmb, float mrplorml, boolean rowMajor) {
        if (rowMajor) {
            writer.float4(torml, 0, 0, mrplorml);
            writer.float4(0, totmb, 0, mtpbotmb);
            writer.float4(0, 0, mtofmn, mfpnofmn);
            writer.float4(0, 0, 0, 1);
        } else {
            writer.float4(torml, 0, 0, 0);
            writer.float4(0, totmb, 0, 0);
            writer.float4(0, 0, mtofmn, 0);
            writer.float4(mrplorml, mtpbotmb, mfpnofmn, 1);
        }
    }
}
