package com.vke.core.parsing.config.schema.vks.parser.tkn;

import com.vke.core.parsing.SourceCursor;
import com.vke.core.parsing.config.schema.vks.parser.VksTokenizer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class VksTokens {
    private final VksTokenizer tokenizer;
    private VksT buffer;

    public VksTokens(VksTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public @Nullable VksT next() throws IOException {
        if (buffer != null) {
            VksT tmp = buffer;
            buffer = null;
            return tmp;
        }
        return tokenizer.nextToken();
    }

    public @Nullable VksT peek() throws IOException {
        if (buffer != null) return buffer;
        buffer = next();
        return buffer;
    }

    public SourceCursor cursor() {
        return tokenizer.cursor();
    }
}
