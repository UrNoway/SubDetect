package net.unelement.sd.ffmpeg.event;

import java.awt.image.BufferedImage;

public class VideoEvent {

    private final long currentMicro;
    private final BufferedImage image;
    private final double fps;
    private final int frame;

    public VideoEvent(long currentMicro, double fps, BufferedImage image) {
        this.currentMicro = currentMicro;
        this.fps = fps;
        this.image = image;
        frame = (int) (currentMicro <= 0L ? 0 : Math.ceil(fps * (double) currentMicro / 1_000_000d));
    }

    public long getCurrentMicro() {
        return currentMicro;
    }

    public BufferedImage getImage() {
        return image;
    }

    public double getFps() {
        return fps;
    }

    public int getFrame() {
        return frame;
    }
}
