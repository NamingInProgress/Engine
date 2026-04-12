package com.vke.core.file.utils;

import com.carrotsearch.hppc.ByteArrayList;
import com.vke.core.file.deflate.compress.DeflatingDevice;
import com.vke.core.file.deflate.decompress.InflatingDevice;
import com.vke.core.file.deflate.decompress.check.Checksum32;
import com.vke.core.file.deflate.exc.InflatingException;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class DataUtils {
    public static int abcd(int a, int b, int c, int d) {
        return (a << 24) | (b << 16) | (c << 8) | d;
    }

    public static int dcba(int a, int b, int c, int d) {
        return (d << 24) | (c << 16) | (b << 8) | a;
    }

    public static int readU8(InputStream stream) throws IOException {
        int i = stream.read();
        return i == -1 ? -1 : i & 0xFF;
    }

    public static int readU16LittleEndian(InputStream stream) throws IOException {
        int a = stream.read();
        int b = stream.read();
        if (a == -1 || b == -1) return -1;
        return abcd(0, 0, b, a);
    }

    public static int readU32LittleEndian(InputStream stream) throws IOException {
        int a = stream.read();
        int b = stream.read();
        int c = stream.read();
        int d = stream.read();
        if (a == -1 || b == -1 || c == -1 || d == -1) return -1;
        return abcd(d, c, b, a);
    }

    public static int readU16BigEndian(InputStream stream) throws IOException {
        int a = stream.read();
        int b = stream.read();
        if (a == -1 || b == -1) return -1;
        return abcd(0, 0, a, b);
    }

    public static int readU32BigEndian(InputStream stream) throws IOException {
        int a = stream.read();
        int b = stream.read();
        int c = stream.read();
        int d = stream.read();
        if (a == -1 || b == -1 || c == -1 || d == -1) return -1;
        return dcba(d, c, b, a);
    }

    public static void writeU8(OutputStream stream, int u8) throws IOException {
        stream.write(u8);
    }

    public static void writeU16LittleEndian(OutputStream stream, int value) throws IOException {
        stream.write(value & 0xFF);
        stream.write((value >> 8) & 0xFF);
    }

    public static void writeU32LittleEndian(OutputStream stream, int value) throws IOException {
        stream.write(value & 0xFF);
        stream.write((value >> 8) & 0xFF);
        stream.write((value >> 16) & 0xFF);
        stream.write((value >> 24) & 0xFF);
    }

    public static void writeU16BigEndian(OutputStream stream, int value) throws IOException {
        stream.write((value >> 8) & 0xFF);
        stream.write(value & 0xFF);
    }

    public static void writeU32BigEndian(OutputStream stream, int value) throws IOException {
        stream.write((value >> 24) & 0xFF);
        stream.write((value >> 16) & 0xFF);
        stream.write((value >> 8) & 0xFF);
        stream.write(value & 0xFF);
    }

    public static long unsign32(int size) {
        return Integer.toUnsignedLong(size);
    }

    public static int sign32(long size) {
        return (int) (size & 0xFFFFFFFFL);
    }

    public static UUID readGuidLittleEndian(InputStream stream) throws IOException {
        long data1 = DataUtils.readU32LittleEndian(stream);
        int data2 = DataUtils.readU16LittleEndian(stream);
        int data3 = DataUtils.readU16LittleEndian(stream);

        byte[] data4 = stream.readNBytes(8);
        if (data4.length != 8) {
            throw new EOFException("Incomplete GUID");
        }

        long msb = (data1 << 32)
                | ((long) data2 << 16)
                | (long) data3;

        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            lsb = (lsb << 8) | (data4[i] & 0xFF);
        }

        return new UUID(msb, lsb);
    }
    
    public static void writeNullStringUTF8(OutputStream stream, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        stream.write(bytes);
        stream.write('\0');
    }
    
    public static String readNullStringUTF8(InputStream stream) throws IOException {
        ByteArrayList bytes = new ByteArrayList();
        int current = stream.read();
        int counter = 1;
        if (current == -1) throw new EOFException();
        while (current != '\0') {
            if (counter >= 65536) {
                throw new IOException("I think your string is kinda infinite...");
            }
            
            bytes.add((byte) current);
            current = stream.read();
            counter++;
            if (current == -1) throw new EOFException();
        }
        return new String(bytes.toArray(), StandardCharsets.UTF_8);
    }

    public static Iter<String> readerLines(Reader reader) {
        return new LineReader(reader);
    }

    private static class LineReader implements Iter<String> {
        private final BufferedReader reader;

        private LineReader(Reader reader) {
            this.reader = new BufferedReader(reader);
        }

        @Override
        public @NotNull Option<String> next() {
            try {
                String line = reader.readLine();
                return line == null ? Option.none() : Option.some(line);
            } catch (IOException e) {
                return Option.none();
            }
        }
    }

    public static byte[] deflate(byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length / 2);
        DeflatingDevice device = new DeflatingDevice(out, 64);
        device.deflateNext(data);
        device.finish();
        return out.toByteArray();
    }

    public static byte[] inflate(byte[] deflated) throws InflatingException {
        return inflate(deflated, null);
    }

    public static byte[] inflate(byte[] deflated, @Nullable Checksum32 checksum) throws InflatingException {
        ByteArrayInputStream in = new ByteArrayInputStream(deflated);
        InflatingDevice device = new InflatingDevice(checksum, in);
        ByteArrayList out = new ByteArrayList(deflated.length);
        while (!device.isFinished()) {
            int n = device.inflateNextByte();
            if (n == -1) break;
            out.add((byte) n);
        }
        return out.toArray();
    }
}
