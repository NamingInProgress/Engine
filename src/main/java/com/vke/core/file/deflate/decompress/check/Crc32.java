package com.vke.core.file.deflate.decompress.check;

public class Crc32 implements Checksum32 {
    private int crc = 0xFFFFFFFF;

    @Override
    public void nextByte(int u8) {
        u8 &= 0xFF;
        /*
        from: https://lxp32.github.io/docs/a-simple-example-crc32-calculation/

        for(size_t j=0;j<8;j++) {
			uint32_t b=(ch^crc)&1;
			crc>>=1;
			if(b) crc=crc^0xEDB88320;
			ch>>=1;
		}

         */

        for (int i = 0; i < 8; i++) {
            int b = (u8 ^ crc) & 1;
            crc >>>= 1;
            if (b == 1) crc = crc ^ 0xEDB88320;
            u8 >>>= 1;
        }
    }

    @Override
    public int get() {
        return ~crc;
    }
}
