package com.vke.core.file.utils;

import com.vke.api.file.DecodeException;
import com.vke.api.file.Decoder;
import com.vke.api.file.LazyArray;
import com.vke.api.file.LazyDecoder;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

public abstract class HBFLazyDecoder<S extends HBFDecodeSource<LazyT>, LazyT, CombinedT> implements LazyDecoder<LazyT>, Decoder<CombinedT> {
    @Override
    public CombinedT decode(InputStream input) throws DecodeException {
        Lazy lazy = new Lazy(fromStream(input));

        startCollecting();

        LazyT next;
        while ((next = lazy.next()) != null) {
            addNext(next);
        }

        return combine();
    }

    @Override
    public LazyArray<LazyT> decodeLazy(InputStream stream) throws DecodeException {
        return new Lazy(fromStream(stream));
    }

    protected abstract S fromStream(InputStream stream);
    protected abstract boolean isFinal(LazyT element);
    protected abstract void startCollecting();
    protected abstract void addNext(LazyT element);
    protected abstract CombinedT combine();

    public class Lazy implements LazyArray<LazyT> {
        protected final S source;
        private int state;

        private Lazy(S source) {
            this.source = source;
        }

        @Override
        public @Nullable LazyT next() throws DecodeException {
            try {
                if (state == 0) {
                    source.parseHeader();
                    state++;
                } else if (state == 1) {
                    LazyT next = source.nextByte();
                    if (!isFinal(next)) {
                        return next;
                    } else {
                        state = 2;
                    }
                }
                if (state == 2) {
                    source.parseFooter();
                    state++;
                }
                return null;
            } catch (IOException e) {
                throw new DecodeException(e);
            }
        }
    }
}
