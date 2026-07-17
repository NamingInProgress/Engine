package com.vke.core.audio.source;

import com.vke.core.audio.pcm.reader.PCMReader;

public interface AudioClip {
    PCMReader createReader();
}
