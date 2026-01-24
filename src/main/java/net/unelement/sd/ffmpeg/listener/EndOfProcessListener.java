package net.unelement.sd.ffmpeg.listener;

import net.unelement.sd.ffmpeg.event.EndEvent;

import java.util.EventListener;

public interface EndOfProcessListener extends EventListener {
    void endReached(EndEvent event);
}
