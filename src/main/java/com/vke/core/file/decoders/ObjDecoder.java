package com.vke.core.file.decoders;

import com.vke.api.file.DecodeException;
import com.vke.api.file.Decoder;
import com.vke.core.file.obj.ObjFile;
import com.vke.utils.Utils;

import java.io.InputStream;

public class ObjDecoder implements Decoder<ObjFile> {
    @Override
    public ObjFile decode(InputStream input) throws DecodeException {
        return Utils.chainExceptions(() -> new ObjFile(input));
    }
}
