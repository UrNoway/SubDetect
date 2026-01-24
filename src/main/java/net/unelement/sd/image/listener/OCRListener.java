package net.unelement.sd.image.listener;

import net.unelement.sd.image.event.OCREvent;

import java.util.EventListener;

public interface OCRListener extends EventListener {
    void updateText(OCREvent event);
}
