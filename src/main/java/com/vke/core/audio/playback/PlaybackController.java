package com.vke.core.audio.playback;

import com.vke.core.audio.device.AudioDevice;
import com.vke.utils.io.Disposable;

public class PlaybackController extends Thread implements Disposable {
    private final AudioDevice audioDevice;
    private final MasterMixer mixer;
    private volatile boolean running;

    public PlaybackController(AudioDevice device) {
        super("AudioPlayback");
        this.audioDevice = device;
        this.mixer = new MasterMixer();
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            float[] mixed = mixer.mixBlock();
            audioDevice.read(mixed, PlaybackState.BLOCK_SIZE);
        }
    }

    public MasterMixer getMixer() {
        return mixer;
    }

    public void cancel() {
        running = false;
    }

    @Override
    public void free() {
        cancel();
        audioDevice.free();
    }
}
