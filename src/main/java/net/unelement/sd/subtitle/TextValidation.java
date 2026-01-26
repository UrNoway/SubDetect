package net.unelement.sd.subtitle;

import net.unelement.sd.subtitle.event.TextValidationEvent;
import net.unelement.sd.subtitle.listener.TextValidationListener;

import javax.swing.event.EventListenerList;
import java.util.*;
import java.util.stream.Stream;

public class TextValidation implements Runnable {

    private volatile boolean running;
    private Thread thread;

    private final double fps;

    private final Map<Long, String> detected;
    private final List<SubtitleEvent> events;

    private SubtitleEvent lastEvent;

    public TextValidation(Map<Long, String> detected, double fps) {
        this.detected = detected;
        this.fps = fps;
        events = new ArrayList<>();
        lastEvent = null;
    }

    public void start() {
        startThread();
    }

    private void startThread() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    private void stopThread() {
        if(thread != null && thread.isAlive()) {
            running = false;
            thread.interrupt();

            fireTextValidationEvent(new TextValidationEvent(events));
        }
    }

    public void process(){
        // Sort the map
        Map<Long, String> sorted = new TreeMap<>(detected);

        // Count the frames
        int counter = 0;

        // Register last text
        String lastStringInMap = "";

        // Correction for end of events (event time ends by wrong value)
        // So we have to add one frame (corresponding in microseconds)
        long correction = Math.round(1d / fps * 1_000_000d);

        // Add to list by applying filter
        for(Map.Entry<Long, String> entry : sorted.entrySet()){
            if(!entry.getValue().trim().equals(lastStringInMap)){
                if (lastEvent != null){
                    events.add(lastEvent);
                }
                if(!entry.getValue().trim().isEmpty()){
                    lastEvent = new SubtitleEvent();
                    lastEvent.setText(entry.getValue().trim());
                    lastEvent.setMicrosStart(entry.getKey());
                }
            }else if(detected.size() - 1 == counter && lastEvent != null){
                lastEvent.setMicrosEnd(lastEvent.getMicrosEnd() + correction);
                events.add(lastEvent);
            }else{
                if(lastEvent != null){
                    lastEvent.setMicrosEnd(entry.getKey() + correction);
                }
            }
            lastStringInMap = entry.getValue().trim();
            counter++;
        }

        stopThread();
    }

    /**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     * <a href="https://stackoverflow.com/questions/955110/similarity-string-comparison-in-java">Stack Overflow Issue</a>
     */
    private double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;

        // longer should always have greater length
        if (s1.length() < s2.length()) {
            longer = s2; shorter = s1;
        }

        int longerLength = longer.length();

        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }

        /*
        If you have Apache Commons Text, you can use it to calculate the edit distance:
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        return (longerLength - levenshteinDistance.apply(longer, shorter)) / (double) longerLength;
        */
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    @Override
    public void run() {
        while(true){
            if(running){
                process();
            }
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

    public void addTextValidationListener(TextValidationListener listener){
        listeners.add(TextValidationListener.class, listener);
    }

    public void removeTextValidationListener(TextValidationListener listener){
        listeners.remove(TextValidationListener.class, listener);
    }

    protected void fireTextValidationEvent(TextValidationEvent message){
        for(Object o : getListeners()){
            if(o instanceof TextValidationListener listen){
                listen.subtitlesReady(message);
                break;
            }
        }
    }
}
