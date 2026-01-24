package net.unelement.sd.ffmpeg.listener;

import net.unelement.sd.ffmpeg.event.VideoEvent;

import java.util.EventListener;

public interface VideoListener extends EventListener {
    void videoFrameUpdated(VideoEvent event);
}
