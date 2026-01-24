package net.unelement.sd.subtitle.listener;

import net.unelement.sd.subtitle.event.TextValidationEvent;

import java.util.EventListener;

public interface TextValidationListener extends EventListener {
    void subtitlesReady(TextValidationEvent event);
}
