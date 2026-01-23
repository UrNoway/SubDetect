package net.unelement.sd.subtitle;

import java.util.List;

public class TextValidationEvent {

    private final List<SubtitleEvent> events;

    public TextValidationEvent(List<SubtitleEvent> events){
        this.events = events;
    }

    public List<SubtitleEvent> getEvents() {
        return events;
    }
}
