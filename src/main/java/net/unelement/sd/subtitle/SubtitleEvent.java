package net.unelement.sd.subtitle;

public class SubtitleEvent {

    private long microsStart;
    private long microsEnd;

    private String text;

    public SubtitleEvent() {
        microsStart = 0L;
        microsEnd = 0L;
        text = "";
    }

    public long getMicrosStart() {
        return microsStart;
    }

    public void setMicrosStart(long microsStart) {
        this.microsStart = microsStart;
    }

    public long getMicrosEnd() {
        return microsEnd;
    }

    public void setMicrosEnd(long microsEnd) {
        this.microsEnd = microsEnd;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
