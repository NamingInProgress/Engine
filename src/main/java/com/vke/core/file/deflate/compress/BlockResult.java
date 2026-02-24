package com.vke.core.file.deflate.compress;

import java.io.ByteArrayOutputStream;

//ill keep it as bao to avoid at least 1 unnecessary copy of the byte array
public record BlockResult(ByteArrayOutputStream byteOutput, int partialBits, int fullBytes) {
}
