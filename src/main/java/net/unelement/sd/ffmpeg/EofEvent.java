package net.unelement.sd.ffmpeg;

public class EofEvent {
    private final long currentMicro;

    public EofEvent(long currentMicro) {
        this.currentMicro = currentMicro;
    }

    public long getCurrentMicro() {
        return currentMicro;
    }
}
