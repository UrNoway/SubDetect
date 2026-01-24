package net.unelement.sd.ffmpeg.event;

public class EndEvent {

    private  final int frame;
    private final long microseconds;

    public EndEvent(int frame, long microseconds) {
        this.frame = frame;
        this.microseconds = microseconds;
    }

    public int getFrame() {
        return frame;
    }

    public long getMicroseconds() {
        return microseconds;
    }
}
