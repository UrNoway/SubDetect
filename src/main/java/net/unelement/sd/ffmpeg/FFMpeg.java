package net.unelement.sd.ffmpeg;

import net.unelement.sd.ffmpeg.event.EndEvent;
import net.unelement.sd.ffmpeg.event.VideoEvent;
import net.unelement.sd.ffmpeg.listener.EndOfProcessListener;
import net.unelement.sd.ffmpeg.listener.VideoListener;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class FFMpeg implements Runnable {

    private volatile Thread playThread;
    private final AtomicBoolean working;
    private final AtomicBoolean running;
    private volatile String media;
    private volatile long startTime;
    private volatile long endTime;
    private final List<Long> keyFrames;
    private long mediaLength;
    private int mediaFrameCount;

    private long positionCounter = -1;

    private volatile FFmpegFrameGrabber grabber = null;
    private volatile ExecutorService imageExecutor = null;
    private final Java2DFrameConverter converter;

    public FFMpeg() {
        playThread = null;
        working = new AtomicBoolean(false);
        running = new AtomicBoolean(false);
        media = null;
        startTime = -1L;
        endTime = -1L;
        keyFrames = new ArrayList<>();
        mediaLength = -1L;
        mediaFrameCount = -1;

        converter = new Java2DFrameConverter();
    }

    public void setMedia(String path){
        media = path;
        if(grabber != null){
            free();
            grabber = null;
        }
        if(path != null){
            grabber = new FFmpegFrameGrabber(path);
            try{
                String ffProbe = Loader.load(org.bytedeco.ffmpeg.ffprobe.class);
                ProcessBuilder pb = new ProcessBuilder(
                        ffProbe,
                        "-i", String.format("\"%s\"", path),
                        "-loglevel", "error",
                        "-select_streams", "v:0",
                        "-show_entries", "packet=pts_time,flags",
                        "-of", "csv=print_section=0");
                Process p = pb.redirectErrorStream(true).start();

                try(BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))){
                    String line;
                    while((line = br.readLine()) != null){
                        if(line.contains("K")){
                            double value = Double.parseDouble(line.substring(0, line.indexOf(",")));
                            keyFrames.add(Math.round(value * 1_000_000d));
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void free(){
        if(grabber != null){
            try {
                grabber.stop();
                grabber.release();
            } catch (FrameGrabber.Exception e) {
                JOptionPane.showMessageDialog(
                        new JFrame(),
                        "Grabber has tried to be free!\n(free() at FFmpeg.java)"
                );
            }
        }
    }

    public void play(){
        if(media == null) return;
        try{
            startThread();
        }catch(Exception _){
            JOptionPane.showMessageDialog(
                    new JFrame(),
                    "Something goes wrong when\nyou are tried to play a media!"
            );
        }
    }

    public void pause(){
        if(media == null) return;
        if (playThread == null) return;
        running.set(!running.get());
    }

    public void stop(){
        if(media == null) return;
        if (playThread == null) return;
        running.set(false);
    }

    public void setStartTime(long micros){
        startTime = micros;
    }

    public void setEndTime(long micros){
        endTime = micros;
    }

    public List<Long> getKeyFrames() {
        return keyFrames;
    }

    public long getMediaLength() {
        return mediaLength;
    }

    public int getMediaFrameCount() {
        return mediaFrameCount;
    }

    @Override
    public void run() {
        while(working.get()){
            if(running.get()){
                // It is bad the media loops.
                // If you don't want to loop then consider modify.
                doProcess();
            }
        }
    }

    public void startThread() {
        stopThread();
        if(playThread == null || (playThread != null &&
                (playThread.isInterrupted() || !running.get()))){
            if(playThread == null){
                playThread = new Thread(this);
                playThread.start();
            }
            working.set(true);
            running.set(true);
        }
    }

    public void stopThread() {
        if(playThread != null && !playThread.isInterrupted()){
            running.set(false);
            working.set(false);
            playThread.interrupt();
            playThread = null;
        }
    }

    private void doProcess(){
        try {
            grabber.start();

            mediaLength = grabber.getLengthInTime();
            mediaFrameCount = grabber.getLengthInVideoFrames();

            imageExecutor = Executors.newSingleThreadExecutor();

            while (!Thread.interrupted()) {
                if(running.get()){

                    positionCounter = Math.max(positionCounter, grabber.getFrameNumber());
                    if(grabber.getFrameNumber() < positionCounter){
                        stop();
                        positionCounter = -1;
                        //fireEofEvent(new EofEvent(grabber.getTimestamp()));
                    }

                    //==============================================
                    // DESIRED START TRIGGER - AREA START
                    //==============================================
                    if(startTime != -1L){
                        grabber.setTimestamp(startTime);
                        startTime = -1L;
                    }
                    //==============================================
                    // DESIRED START TRIGGER - AREA END
                    //==============================================
                    final Frame frame = grabber.grab();
                    if (frame == null) {
                        break;
                    }

//                    if(grabber.getFrameNumber() >= grabber.getLengthInVideoFrames()){
//                        fireEndOfProcessEvent(new EndEvent(
//                                grabber.getFrameNumber(),
//                                grabber.getTimestamp())
//                        );
//                    }

                    //==============================================
                    // DESIRED END TRIGGER - AREA START
                    //==============================================
                    if((frame.image != null || frame.samples != null)
                            && !playThread.isInterrupted() && endTime != -1L){
                        if(frame.timestamp >= endTime){
                            endTime = -1L;
                            running.set(false);
                            continue;
                        }
                    }
                    //==============================================
                    // DESIRED END TRIGGER - AREA END
                    //==============================================
                    if (frame.image != null) {
                        final Frame imageFrame = frame.clone();

                        imageExecutor.submit(() -> {
                            final BufferedImage image = converter.convert(imageFrame);
                            imageFrame.close();

                            //==============================================
                            // EVENT TRIGGER - AREA START
                            //==============================================

                            VideoEvent m = new VideoEvent(
                                    imageFrame.timestamp,
                                    grabber.getFrameRate(),
                                    image
                            );

                            fireVideoUpdateEvent(m);

                            //==============================================
                            // EVENT TRIGGER - AREA END
                            //==============================================
                        });
                    }
                }

            }

            free();

        } catch (Exception _) {

        }
    }

    //=======================================================
    //=======================================================
    //-------------------------------------------------------
    // EVENTS
    //=======================================================

    private final EventListenerList listeners = new EventListenerList();

    public Object[] getListeners(){
        return listeners.getListenerList();
    }

    public void addMediaListener(Object o){
        switch(o){
            case VideoListener x -> listeners.add(VideoListener.class, x);
            case EndOfProcessListener x -> listeners.add(EndOfProcessListener.class, x);
            default -> {}
        }
    }
    public void removeMediaListener(Object o){
        switch(o){
            case VideoListener x -> listeners.remove(VideoListener.class, x);
            case EndOfProcessListener x -> listeners.remove(EndOfProcessListener.class, x);
            default -> {}
        }
    }

    protected void fireVideoUpdateEvent(VideoEvent message){
        for(Object o : getListeners()){
            if(o instanceof VideoListener listen){
                listen.videoFrameUpdated(message);
                break;
            }
        }
    }

    protected void fireEndOfProcessEvent(EndEvent message){
        for(Object o : getListeners()){
            if(o instanceof EndOfProcessListener listen){
                listen.endReached(message);
                break;
            }
        }
    }
}