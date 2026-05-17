package com.vke.core.assets.pipeline.protocols.shader;

import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;
import com.vke.utils.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ShaderPreprocessor {

    private static ShaderPreprocessor instance;

    public static ShaderPreprocessor getInstance() {
        if (instance == null) instance = new ShaderPreprocessor();
        return instance;
    }

    public Pair<String, ShaderMetadata> process(Identifier ident) throws IOException {
        String code = Utils.readStringFromInputStream(ident.asInputStream());
        code = code.replace("#MultipleWrites(100)", "");
        code = code.replace("#Static", "");

        return new Pair<>(code, new ShaderMetadata(new HashMap<>(), new ArrayList<>()));
    }

    public record ShaderMetadata(HashMap<String, Integer> multipleWrites, ArrayList<String> staticBuffers) {}

}
