package net.unelement.sd.subtitle;

import java.util.EventListener;

public interface TextValidationListener extends EventListener {
    void subtitlesReady(TextValidationEvent event);
}
