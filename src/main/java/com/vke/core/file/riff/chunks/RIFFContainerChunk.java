package com.vke.core.file.riff.chunks;

import com.vke.core.file.riff.RIFFChunk;
import com.vke.core.file.riff.RIFFFormat;
import com.vke.core.file.riff.RIFFPayload;
import com.vke.core.file.utils.Ascii4;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class RIFFContainerChunk extends RIFFChunk {
    protected final Ascii4 type;
    protected final List<RIFFChunk> subChunks;

    public RIFFContainerChunk(Ascii4 name, InputStream stream, RIFFFormat format) throws IOException {
        super(name, stream, format);
        Payload payload = (Payload) this.payload;
        this.type = payload.type;
        this.subChunks = payload.subChunks;
    }

    @Override
    protected RIFFPayload readPayload(InputStream stream, RIFFFormat format) throws IOException {
        Ascii4 type = Ascii4.read(stream);
        List<RIFFChunk> chunks = new ArrayList<>();

        long consumed = 4; //name

        while (consumed < size) {
            RIFFChunk next = format.readNextChunk(stream);
            chunks.add(next);
            consumed += 8 + next.actualSize() + 4;
        }

        return new Payload(type, chunks);
    }

    public List<RIFFChunk> getSubChunks() {
        return subChunks;
    }

    public Ascii4 getType() {
        return type;
    }

    public Iterator<RIFFChunk> findChunksByName(Ascii4 name) {
        return new Finder(name);
    }

    public static class Payload extends RIFFPayload {
        private final Ascii4 type;
        private final List<RIFFChunk> subChunks;

        public Payload(Ascii4 type, List<RIFFChunk> subChunks) {
            this.type = type;
            this.subChunks = subChunks;
        }

        public Ascii4 getType() {
            return type;
        }

        public List<RIFFChunk> getSubChunks() {
            return subChunks;
        }
    }

    public class Finder implements Iterator<RIFFChunk> {
        private final Ascii4 query;
        private int index;
        private RIFFChunk cache;

        public Finder(Ascii4 query) {
            this.query = query;
            cache();
        }

        @Override
        public boolean hasNext() {
            return cache != null;
        }

        @Override
        public RIFFChunk next() {
            RIFFChunk ret = cache;
            cache();
            return ret;
        }

        private void cache() {
            for (;index < subChunks.size(); index++) {
                RIFFChunk candidate = subChunks.get(index);
                if (candidate.name().equals(query)) {
                    cache = candidate;
                    break;
                }
            }
        }
    }
}
