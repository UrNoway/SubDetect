package net.unelement.sd.image;

public class OCREvent {

    private final String text;
    private final long microseconds;
    private final int currentFrame;
    private final int frameCount;

    public OCREvent(String text, long microseconds, int currentFrame, int frameCount) {
        this.text = text;
        this.microseconds = microseconds;
        this.currentFrame = currentFrame;
        this.frameCount = frameCount;
    }

    public String getText() {
        return text;
    }

    public long getMicroseconds() {
        return microseconds;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public int getFrameCount() {
        return frameCount;
    }
}
