package net.unelement.sd.image;

import java.util.EventListener;

public interface OCRListener extends EventListener {
    void updateText(OCREvent event);
}
