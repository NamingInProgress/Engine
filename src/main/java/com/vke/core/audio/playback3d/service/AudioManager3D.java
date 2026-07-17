package com.vke.core.audio.playback3d.service;

import com.vke.api.services2.Service;
import com.vke.core.Context;
import com.vke.core.audio.playback3d.Ear;
import com.vke.core.audio.playback3d.Speaker;

public interface AudioManager3D extends Service {
    Ear createEar();
    Speaker createSpeaker();
    Speaker createSpeaker(Context context);

    void setListeningEar(Ear ear);
}
