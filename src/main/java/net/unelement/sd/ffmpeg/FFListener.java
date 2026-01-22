package net.unelement.sd.ffmpeg;

import java.util.EventListener;

public interface FFListener extends EventListener {
    void updated(FFEvent event);
    void endOfFile(EofEvent event);
}
