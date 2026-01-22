package net.unelement.sd.image;

public class OCREvent {

    private final String text;
    private final long microseconds;

    public OCREvent(String text, long microseconds) {
        this.text = text;
        this.microseconds = microseconds;
    }

    public String getText() {
        return text;
    }

    public long getMicroseconds() {
        return microseconds;
    }
}
